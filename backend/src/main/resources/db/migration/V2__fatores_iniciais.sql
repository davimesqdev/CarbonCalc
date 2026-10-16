-- Fatores de emissão de referência (kg CO2e por unidade). Valores ilustrativos,
-- usados como cache local quando a fonte externa está indisponível.
insert into fator_emissao (tipo_fonte, valor, unidade, vigencia_inicio) values
    ('DIESEL',                  2.603000, 'L',             date '2025-01-01'),
    ('GASOLINA',                2.212000, 'L',             date '2025-01-01'),
    ('GAS_NATURAL',             2.066000, 'm³',            date '2025-01-01'),
    ('GLP',                     2.932000, 'kg',            date '2025-01-01'),
    ('ELETRICIDADE',            0.054500, 'kWh',           date '2025-01-01'),
    ('VIAGEM_AEREA',            0.110000, 'passageiro.km', date '2025-01-01'),
    ('TRANSPORTE_TERCEIRIZADO', 0.100000, 't.km',          date '2025-01-01'),
    ('RESIDUOS_ATERRO',         0.450000, 'kg',            date '2025-01-01');
