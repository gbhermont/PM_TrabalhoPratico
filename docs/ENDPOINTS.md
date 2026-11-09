# Endpoints da API

Base: aplicação Spring Boot (porta padrão 8080). Erros padronizados via `ErroResposta`
(400 = regra de negócio/validação/filtro inválido, 404 = recurso não encontrado).

## /pacientes

| Método | Rota             | Status sucesso | Erros        |
|--------|------------------|----------------|--------------|
| POST   | /pacientes       | 201 + Location | 400          |
| GET    | /pacientes       | 200            | -            |
| GET    | /pacientes/{id}  | 200            | 404          |
| PUT    | /pacientes/{id}  | 200            | 400, 404     |
| DELETE | /pacientes/{id}  | 204            | 404          |

## /profissionais

| Método | Rota                             | Status sucesso | Erros        |
|--------|----------------------------------|----------------|--------------|
| POST   | /profissionais                   | 201 + Location | 400          |
| GET    | /profissionais?especialidade=    | 200            | 400 (filtro) |
| GET    | /profissionais/{id}              | 200            | 404          |
| PUT    | /profissionais/{id}              | 200            | 400, 404     |
| DELETE | /profissionais/{id}              | 204            | 404          |
| POST   | /profissionais/{id}/procedimentos| 201            | 400, 404     |
| GET    | /profissionais/{id}/procedimentos| 200            | 400, 404     |

## /consultas (atendimentos: rotina, especializada, emergência, cirúrgico)

| Método | Rota                              | Status sucesso | Erros        |
|--------|-----------------------------------|----------------|--------------|
| POST   | /consultas                        | 201 + Location | 400, 404     |
| GET    | /consultas?tipo=&pacienteId=&profissionalId=&situacao= | 200 | 400 (filtro) |
| GET    | /consultas/{id}                   | 200            | 404          |
| PATCH  | /consultas/{id}/situacao          | 200            | 400, 404     |
| POST   | /consultas/{id}/encaminhamento    | 201 + Location | 400, 404     |

## /internacoes

| Método | Rota                        | Status sucesso | Erros        |
|--------|-----------------------------|----------------|--------------|
| POST   | /internacoes                | 201 + Location | 400, 404     |
| GET    | /internacoes?pacienteId=    | 200            | 404 (paciente)|
| GET    | /internacoes/{id}           | 200            | 404          |
| PUT    | /internacoes/{id}/alta      | 200            | 400, 404     |

Sem DELETE: o histórico de internação não é apagado.

## /quartos

| Método | Rota                        | Status sucesso | Erros        |
|--------|-----------------------------|----------------|--------------|
| POST   | /quartos                    | 201 + Location | 400          |
| GET    | /quartos?tipo=&situacao=    | 200            | 400 (filtro) |
| GET    | /quartos/{id}               | 200            | 404          |
| PUT    | /quartos/{id}               | 200            | 400, 404     |
| DELETE | /quartos/{id}               | 204            | 400, 404     |
