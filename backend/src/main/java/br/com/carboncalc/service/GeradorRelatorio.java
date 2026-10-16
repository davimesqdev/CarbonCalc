package br.com.carboncalc.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import br.com.carboncalc.domain.Calculo;
import br.com.carboncalc.domain.Compensacao;
import br.com.carboncalc.domain.Empresa;
import br.com.carboncalc.domain.Formato;
import br.com.carboncalc.domain.TipoRelatorio;

/** Monta o conteúdo dos relatórios em PDF e Excel. */
@Component
public class GeradorRelatorio {

    private static final Locale PT_BR = Locale.of("pt", "BR");
    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private static final Font FONTE_TITULO = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
    private static final Font FONTE_SECAO = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
    private static final Font FONTE_TEXTO = FontFactory.getFont(FontFactory.HELVETICA, 10);
    private static final Font FONTE_NEGRITO = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);

    /** {@code compensacoes} só entra no relatório do tipo plano de compensação. */
    public byte[] gerar(TipoRelatorio tipo, Formato formato, Calculo calculo, List<Compensacao> compensacoes) {
        List<Compensacao> acoes = tipo == TipoRelatorio.PLANO_COMPENSACAO ? compensacoes : List.of();
        return switch (formato) {
            case PDF -> gerarPdf(tipo, calculo, acoes);
            case XLSX -> gerarXlsx(tipo, calculo, acoes);
        };
    }

    private byte[] gerarPdf(TipoRelatorio tipo, Calculo calculo, List<Compensacao> acoes) {
        Empresa empresa = calculo.getEmpresa();
        ByteArrayOutputStream saida = new ByteArrayOutputStream();
        Document documento = new Document(PageSize.A4, 48, 48, 56, 56);
        PdfWriter.getInstance(documento, saida);
        documento.open();

        documento.add(new Paragraph("CarbonCalc — " + tipo.getRotulo(), FONTE_TITULO));
        documento.add(new Paragraph(" "));
        documento.add(new Paragraph("Empresa: " + empresa.getNome() + " (CNPJ " + empresa.getCnpj() + ")", FONTE_TEXTO));
        documento.add(new Paragraph("Setor: " + empresa.getSetor(), FONTE_TEXTO));
        documento.add(new Paragraph("Cenário: " + calculo.getCenario(), FONTE_TEXTO));
        documento.add(new Paragraph("Período: " + periodo(calculo), FONTE_TEXTO));
        documento.add(new Paragraph("Calculado em " + DATA_HORA.format(calculo.getDataCalculo())
                + " (fatores: " + origem(calculo) + ")", FONTE_TEXTO));

        documento.add(secao("Emissões por escopo (t CO2e)"));
        PdfPTable emissoes = tabela(2, "Escopo", "Emissões (t CO2e)");
        linha(emissoes, "Escopo 1 — emissões diretas", numero(calculo.getEscopo1()));
        linha(emissoes, "Escopo 2 — energia elétrica", numero(calculo.getEscopo2()));
        linha(emissoes, "Escopo 3 — cadeia de valor", numero(calculo.getEscopo3()));
        emissoes.addCell(celula("Total", FONTE_NEGRITO, Element.ALIGN_LEFT));
        emissoes.addCell(celula(numero(calculo.getTotalCO2e()), FONTE_NEGRITO, Element.ALIGN_RIGHT));
        documento.add(emissoes);

        if (!acoes.isEmpty()) {
            documento.add(secao("Plano de compensação"));
            PdfPTable plano = tabela(4, "Ação", "Créditos (t CO2e)", "Quantidade", "Custo estimado");
            for (Compensacao acao : acoes) {
                plano.addCell(celula(acao.getTipoAcao().getRotulo(), FONTE_TEXTO, Element.ALIGN_LEFT));
                plano.addCell(celula(numero(acao.getCreditosNecessarios()), FONTE_TEXTO, Element.ALIGN_RIGHT));
                plano.addCell(celula(numero(acao.getQuantidade()) + " " + acao.getUnidade(), FONTE_TEXTO,
                        Element.ALIGN_RIGHT));
                plano.addCell(celula(moeda(acao.getCustoEstimado()), FONTE_TEXTO, Element.ALIGN_RIGHT));
            }
            documento.add(plano);
            documento.add(new Paragraph("Custos e rendimentos são estimativas ilustrativas.", FONTE_TEXTO));
        }

        documento.close();
        return saida.toByteArray();
    }

    private byte[] gerarXlsx(TipoRelatorio tipo, Calculo calculo, List<Compensacao> acoes) {
        Empresa empresa = calculo.getEmpresa();
        try (XSSFWorkbook planilha = new XSSFWorkbook(); ByteArrayOutputStream saida = new ByteArrayOutputStream()) {
            Sheet resumo = planilha.createSheet("Resumo");
            int n = 0;
            texto(resumo, n++, "Relatório", tipo.getRotulo());
            texto(resumo, n++, "Empresa", empresa.getNome());
            texto(resumo, n++, "CNPJ", empresa.getCnpj());
            texto(resumo, n++, "Setor", empresa.getSetor());
            texto(resumo, n++, "Cenário", calculo.getCenario());
            texto(resumo, n++, "Período", periodo(calculo));
            texto(resumo, n++, "Fatores", origem(calculo));
            n++;
            valor(resumo, n++, "Escopo 1 (t CO2e)", calculo.getEscopo1());
            valor(resumo, n++, "Escopo 2 (t CO2e)", calculo.getEscopo2());
            valor(resumo, n++, "Escopo 3 (t CO2e)", calculo.getEscopo3());
            valor(resumo, n, "Total (t CO2e)", calculo.getTotalCO2e());
            resumo.setColumnWidth(0, 24 * 256);
            resumo.setColumnWidth(1, 40 * 256);

            if (!acoes.isEmpty()) {
                Sheet plano = planilha.createSheet("Compensação");
                Row cabecalho = plano.createRow(0);
                String[] colunas = {"Ação", "Créditos (t CO2e)", "Quantidade", "Unidade", "Custo estimado (R$)"};
                for (int i = 0; i < colunas.length; i++) {
                    cabecalho.createCell(i).setCellValue(colunas[i]);
                    plano.setColumnWidth(i, 24 * 256);
                }
                int linha = 1;
                for (Compensacao acao : acoes) {
                    Row row = plano.createRow(linha++);
                    row.createCell(0).setCellValue(acao.getTipoAcao().getRotulo());
                    row.createCell(1).setCellValue(acao.getCreditosNecessarios().doubleValue());
                    row.createCell(2).setCellValue(acao.getQuantidade().doubleValue());
                    row.createCell(3).setCellValue(acao.getUnidade());
                    row.createCell(4).setCellValue(acao.getCustoEstimado().doubleValue());
                }
            }
            planilha.write(saida);
            return saida.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static void texto(Sheet sheet, int linha, String rotulo, String valor) {
        Row row = sheet.createRow(linha);
        row.createCell(0).setCellValue(rotulo);
        row.createCell(1).setCellValue(valor);
    }

    private static void valor(Sheet sheet, int linha, String rotulo, BigDecimal valor) {
        Row row = sheet.createRow(linha);
        row.createCell(0).setCellValue(rotulo);
        row.createCell(1).setCellValue(valor.doubleValue());
    }

    private static Paragraph secao(String titulo) {
        Paragraph paragrafo = new Paragraph(titulo, FONTE_SECAO);
        paragrafo.setSpacingBefore(18);
        paragrafo.setSpacingAfter(8);
        return paragrafo;
    }

    private static PdfPTable tabela(int colunas, String... cabecalhos) {
        PdfPTable tabela = new PdfPTable(colunas);
        tabela.setWidthPercentage(100);
        for (String cabecalho : cabecalhos) {
            tabela.addCell(celula(cabecalho, FONTE_NEGRITO, Element.ALIGN_LEFT));
        }
        return tabela;
    }

    private static void linha(PdfPTable tabela, String rotulo, String valor) {
        tabela.addCell(celula(rotulo, FONTE_TEXTO, Element.ALIGN_LEFT));
        tabela.addCell(celula(valor, FONTE_TEXTO, Element.ALIGN_RIGHT));
    }

    private static PdfPCell celula(String texto, Font fonte, int alinhamento) {
        PdfPCell celula = new PdfPCell(new Phrase(texto, fonte));
        celula.setHorizontalAlignment(alinhamento);
        celula.setPadding(6);
        return celula;
    }

    private static String periodo(Calculo calculo) {
        return DATA.format(calculo.getPeriodoInicio()) + " a " + DATA.format(calculo.getPeriodoFim());
    }

    private static String origem(Calculo calculo) {
        return switch (calculo.getOrigemFatores()) {
            case EXTERNO -> "fonte externa";
            case CACHE_LOCAL -> "cache local";
        };
    }

    private static String numero(BigDecimal valor) {
        NumberFormat formato = NumberFormat.getNumberInstance(PT_BR);
        formato.setMaximumFractionDigits(3);
        return formato.format(valor);
    }

    private static String moeda(BigDecimal valor) {
        return NumberFormat.getCurrencyInstance(PT_BR).format(valor);
    }
}
