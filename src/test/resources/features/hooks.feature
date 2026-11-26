@technical-local
Feature: Integración técnica de hooks API

  Scenario: Inicializa un actor API sin memoria heredada
    Given el escenario API está inicializado
    When el actor técnico recuerda "valor-escenario-uno"
    Then el actor API tiene CallAnApi y puede recordar el valor

  Scenario: El escenario siguiente empieza con memoria limpia
    Given el escenario API está inicializado
    Then el actor API no hereda memoria del escenario anterior
