# Methode Article Internal Components Mapper
Methode Article Internal Components Mapper is a Dropwizard application which consumes Kafka events and maps raw Methode articles to internal content components.

## Introduction
This application depends on the following micro-services:

* kafka-proxy;
* methode-article-mapper.

## Running

`java -jar target/methode-article-internal-components-mapper-service-1.0-SNAPSHOT.jar server methode-article-internal-components-mapper.yaml`

## Endpoints

### Posting content to be mapped

Transformation can be triggered through a POST message containing a Methode article to http://localhost:11070/map
In case the required transformation is triggered to provide an article preview, you need to set a `preview` query parameter in the URL with `true` as value: 
e.g., http://localhost:11070/map?preview=true 
This `preview` setting will not trigger an exception in case of empty article body.

For backwards compatibility reasons, there is also an alias for the above endpoint: `/content-transform/{id}`.
 
### Internals

For article validation the service leverages on methode article mapper's `/map` endpoint. 

### Healthcheck

A GET request to http://localhost:11071/healthcheck or http://localhost:11070/__health

## Example of transformation output 
You can find an example of a transformed article below. 

```
{
  "topper": {
    "theme": "full-bleed-image-left",
    "backgroundColor": "slate",
    "headline": "This is the topper headline Lorem ipsum do",
    "standfirst": "This is the topper standfirst",
    "images": [
      {
        "id": "9aa35da4-df1b-11e6-bd9b-bb10c5678253",
        "type": "square"
      },
      {
        "id": "ae19a456-df1b-11e6-bd9b-bb10c5678253",
        "type": "standard"
      },
      {
        "id": "939f3f1e-df1b-11e6-bd9b-bb10c5678253",
        "type": "wide"
      }
    ]
  },
  "uuid": "5c12d14c-e898-11e6-bbc7-0a523b37d01c",
  "lastModified": 1486038495897,
  "publishReference": "tid_7gccmallzk"
}
```
