# Meu Uber/99 Pro — experimento Android inspirado no conceito de semáforo

Este diretório **não substitui** o site existente. A versão web na raiz permanece como está, com seus lançamentos, histórico e backup. Este é um **protótipo Android independente**, numa branch de desenvolvimento.

## Objetivo

Quando uma oferta da Uber Driver ou da 99 Motorista apresentar valor, quilômetros e minutos em texto acessível, avaliar localmente e mostrar **CORRIDA BOA** (verde) ou **CORRIDA RUIM** (vermelho) em uma faixa sobre a tela. O aviso nunca deve tocar em botões de aceitar/recusar ou controlar o Waze. Inspirado no **conceito público** de semáforo de valores do GigU, **sem copiar código, marca ou interface proprietária**.

## Retomada após o bloqueio do Google Play Protect

O Play Protect impediu a instalação do primeiro APK de teste porque ele declarava um serviço de Acessibilidade capaz de acessar conteúdo de tela. **Não desative o Play Protect, não altere as configurações de segurança e não tente forçar a instalação.**

O projeto agora possui **duas variantes**, compiladas separadamente:
- **`demoDebug`**: app independente `br.com.meuuber99pro.copiloto.demo`, sem registro de AccessibilityService no manifesto, sem leitura de ofertas de outros apps e sem janela sobreposta. Tem campos manuais e mostra as contas de uma oferta para testes. O GitHub Actions gera **somente esta variante** e verifica o manifesto do APK no processo. Ainda pode ser bloqueado por outros motivos; só o teste do dispositivo e eventual distribuição oficial poderão confirmar a instalação.
- **`automaticoDebug`**: conserva o leitor experimental de ofertas e as solicitações de Acessibilidade, mas **não é distribuído** pelo fluxo automatizado. Essa variante não deve ser instalada no celular principal antes de passar por revisão de segurança, de consentimento, de políticas e de testes apropriados.

**Importante:** retirar a permissão sensível da demonstração não equivale a implementar a leitura automática. O objetivo de análise automática depende de uma abordagem aprovada pelo Android/Google Play e pelos termos das plataformas, ainda não comprovada.

## Caminho de distribuição e validação

1. Conferir a compilação e as permissões efetivas do APK de demonstração pelo GitHub Actions.
2. Revisar o código e validar o comportamento em um dispositivo de testes. Não considerar apenas o aviso do Play Protect como prova de malware ou inocuidade.
3. Preparar lançamento interno/fechado via Google Play usando uma identidade de app e assinatura de distribuição adequadas, quando o cadastro da Play Console estiver pronto.
4. Investigar com segurança e consentimento como as ofertas realmente aparecem e quais dados são disponibilizados legalmente, sem capturar ou guardar dados de passageiros.
5. Se a leitura automática por Acessibilidade for realmente necessária e permitida, incluir explicação destacada no app, consentimento explícito, descrição na Play Store e declaração/revisão correspondente no Play Console. Não há garantia de aprovação.

Documentação oficial de referência:
- https://support.google.com/googleplay/android-developer/answer/10964491?hl=pt-BR
- https://support.google.com/googleplay/android-developer/answer/16558241?hl=pt-BR

## Estado real deste protótipo (14/11/2026)

- [x] Projeto Android em Kotlin criado em pasta separada.
- [x] Tela de autorização com explicação sobre acesso às ofertas.
- [x] Serviço limitado aos identificadores da Uber Driver (`com.ubercab.driver`) e 99 Motorista (`com.app99.driver`).
- [x] Parser **conservador** para textos com rótulos separados `Busca ... km ... min` e `Viagem ... km ... min`; layouts diferentes são ignorados.
- [x] Motor financeiro com metas de R$ 40/h brutos, R$ 25/h de sobra e R$ 1/km de sobra.
- [x] Custos provisórios iguais aos do site: etanol a R$ 3,62/l; 9 km/l; R$ 0,25/km de desgaste; financiamento de R$ 2.252/mês; referência de 300 km × dias no mês.
- [x] Aviso sobreposto via `TYPE_ACCESSIBILITY_OVERLAY`, não interativo, com ocultação automática após 8 segundos.
- [x] Tela de demonstração com dados fictícios.
- [x] Compilação do APK automático de depuração ocorreu no CI anteriormente, porém ele foi bloqueado pelo Play Protect ao tentar instalar.
- [ ] Nova variante manual sem Acessibilidade precisa de compilação e validação do manifesto pelo CI.
- [ ] Ainda não há APK validado em aparelho real.
- [ ] Validação da extração dos formatos reais de ofertas da Uber e da 99.
- [ ] Sincronização das preferências editáveis da versão web com a versão Android.
- [ ] Revisão de política do Google Play, testes de acessibilidade e distribuição.

## Dados e privacidade

O usuário deve marcar autorização dentro do app e ativar manualmente o serviço nas configurações de Acessibilidade do Android. O app não solicita INTERNET, não grava dados da oferta, não toca na tela por conta própria, não ativa permissões sozinho e não lê outras aplicações. Não usar o atributo `isAccessibilityTool=true`: este é um auxiliar financeiro para motoristas, **não** uma ferramenta para pessoas com deficiência.

Para publicação no Google Play, documentar o uso da AccessibilityService na ficha do aplicativo e preencher a declaração da API, incluindo divulgação proeminente e consentimento afirmativo no próprio app. Também verificar regras da Uber e 99 antes de disponibilizar comercialmente. Referência: https://support.google.com/googleplay/android-developer/answer/10964491?hl=pt-BR

## Limites da análise

O parser só retorna oferta se conseguir identificar **um único valor monetário e duas seções rotuladas com km e tempo**. Caso contrário, o serviço não exibe veredito. A precisão depende do conteúdo que o Android disponibiliza. Em telas com texto inacessível, layouts diferentes, várias tarifas, corrida com parada, pedágio, bônus e outros casos não previstos, será necessário desenvolver suporte específico e testar cuidadosamente.

A falta de análise **não** significa corrida ruim nem boa. Cálculos provisórios não substituem avaliação financeira completa.

## Como construir e testar

Abrir a pasta `android-copiloto` no Android Studio com SDK Android 35 e JDK 17; sincronizar o Gradle e compilar `:app:assembleDemoDebug`. O repositório ainda não tem Gradle Wrapper e o APK não foi testado. A versão demo não tem serviço de Acessibilidade. Reservar qualquer variante de leitura automática para auditoria e testes controlados, somente após revisão de permissões e políticas; nunca depender dela numa corrida real antes da validação.

### Cenário fictício, 30 dias no mês

- Oferta R$ 30,00; busca 2 km e 5 min; viagem 8 km e 25 min → 10 km, 30 min, R$ 60/h brutos, R$ 20,98 de sobra, R$ 41,95/h de sobra. **Boa**.
- Mesma distância e tempo, oferta R$ 20,00 → R$ 40/h brutos, mas somente R$ 21,95/h de sobra. **Ruim**, embora a meta bruta tenha sido atingida.

Como os meses têm 28 a 31 dias, o rateio real muda com o calendário e esses números são exemplos para mês de 30 dias.

## Próxima validação essencial

Conferir em um Android real se as ofertas da Uber e da 99 expõem valor, distância de busca, distância de viagem e duração de ambas as etapas em nós acessíveis. Sem isso, a leitura automática não pode ser garantida. Não coletar capturas de tela com nomes/endereços de passageiros.
