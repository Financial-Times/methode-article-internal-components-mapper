# UPP - Methode Article Internal Components Mapper

Methode Article Internal Components Mapper is a Dropwizard application which consumes Kafka events and maps raw Methode
articles to internal content components.

## Code

up-maicm

## Primary URL

<https://upp-prod-delivery-glb.upp.ft.com/__methode-article-internal-components-mapper>

## Service Tier

Platinum

## Lifecycle Stage

Production

## Delivered By

content

## Supported By

content

## Known About By

- dimitar.terziev
- hristo.georgiev
- elitsa.pavlova
- elina.kaneva
- georgi.kazakov
- kalin.arsov
- ivan.nikolov
- miroslav.gatsanoga
- mihail.mihaylov
- tsvetan.dimitrov
- georgi.ivanov

## Host Platform

AWS

## Architecture

The transformed content components are put back to Kafka.
Methode-article-mapper and public-concordances-api are used to validate the article if it's valid, and linked content in the body.
Document-store-api is used for both article validation and to resolve Content Placeholder UUIDs, that are pointing to and
FT blog, to the original Wordpress UUID.

## Contains Personal Data

No

## Contains Sensitive Data

No

## Dependencies

- kafka-proxy
- up-mam
- document-store-api
- public-concordances-api

## Failover Architecture Type

ActiveActive

## Failover Process Type

FullyAutomated

## Failback Process Type

FullyAutomated

## Failover Details

The service is deployed in both Delivery clusters.
The failover guide for the cluster is located here:
<https://github.com/Financial-Times/upp-docs/tree/master/failover-guides/delivery-cluster>

## Data Recovery Process Type

NotApplicable

## Data Recovery Details

The service does not store data, so it does not require any data recovery steps.

## Release Process Type

PartiallyAutomated

## Rollback Process Type

Manual

## Release Details

Manual failover is needed when a new version of
the service is deployed to production.
Otherwise, an automated failover is going to take place when releasing.
For more details about the failover process please see: <https://github.com/Financial-Times/upp-docs/tree/master/failover-guides/delivery-cluster>

## Key Management Process Type

Manual

## Key Management Details

To access the service clients need to provide basic auth credentials.
To rotate credentials you need to login to a particular cluster and update varnish-auth secrets.

## Monitoring

Service in UPP K8S delivery clusters:

- Pub-Prod-EU health: <https://upp-prod-delivery-eu.upp.ft.com/__health/__pods-health?service-name=methode-article-internal-components-mapper>
- Pub-Prod-US health: <https://upp-prod-delivery-us.upp.ft.com/__health/__pods-health?service-name=methode-article-internal-components-mapper>

## First Line Troubleshooting

<https://github.com/Financial-Times/upp-docs/tree/master/guides/ops/first-line-troubleshooting>

## Second Line Troubleshooting

Please refer to the GitHub repository README for troubleshooting information.
