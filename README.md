# send-letter-mock

## Purpose
Send Letter Mock is a locally run app for testing the various dev environments of the Send Letter Service
using Send Letter Client API.

## Getting Started
### Prerequisites

- [JDK 21](https://www.oracle.com/java)
- Project requires Spring Boot v3.x to be present

## Building and deploying the application

### Installation
- Clone the repository

You can also install the project using the common-dev-env-bsbp.
See [common-dev-env-bsbp](https://github.com/hmcts/common-dev-env-bsbp) repository for more information.


### Enviroment Variables and Settings
Several environment variables need to be set to run this application.

| Variable                             | Where to set it | Where do get the value                                                                                                               |
|--------------------------------------|-----------------|--------------------------------------------------------------------------------------------------------------------------------------|
| IDAM_S2S_AUTH_TOTP_SECRET            | .env file       | Service Azure Key vault                                                                                                              |
| IDAM_S2S_AUTH_MICROSERVICE           | .env file       | Service Azure Key Vault                                                                                                              |
| AUTH_PROVIDER_SERVICE_CLIENT_BASEURL | .env file       | Service Azure Key Vault                                                                                                              |
| SEND_LETTER_URL                      | .env file       | `http://rpe-send-letter-service-{env}.service.core-compute-{env}.internal` replace {env} with environment you want to test e.g. demo |

> ℹ️ Hint: For getting the value look at the infrastructure values in a service's (e.g. Private Law)
> code repository for the variable's alias and then look for that alias in that service's key vault

> ℹ️ Hint: The service has different values for each environment so match the variable value to
> whatever Send Letter environment you are testing i.e. if you are testing Send Letter demo grab the
> service's demo key vault values.

### Building the application

The project uses [Gradle](https://gradle.org) as a build tool. It already contains
`./gradlew` wrapper script, so there's no need to install gradle.

To build the project execute the following command:

```bash
  ./gradlew build
```

### Running the application

Once you have set the environment variables:

1. Make sure the application run config is using the .env file. In IntelliJ, you can do this by
right clicking the `Application` class -> More Run/Debug -> Edit Run Configuration -> Selecting Enable EnvFile
-> Click the + symbol and navigate to the .env file and select it.
2. Run the application. In IntelliJ, by right clicking application class and clicking Run Application

### Testing Send Letter

The testing endpoint creates the letter and fills in all information. This info can be customised in the `BulkPrintService`
class e.g. you could change the additional data sent in the request to Send Letter. There is one PDF in the resources folder called test_pdf.pdf, more can be added if needed.

Once you have the application up and running:

1. Hit the testing endpoint by performing a GET request to http://localhost:8877/test
2. You should receive a letter ID back
3. Check this letter ID against the Send Letter database

### Running the application (Docker alternative)

Create the image of the application by executing the following command:

```bash
  ./gradlew assemble
```

Create docker image:

```bash
  docker-compose build
```

Run the distribution (created in `build/install/send-letter-mock` directory)
by executing the following command:

```bash
  docker-compose up
```

This will start the API container exposing the application's port
(set to `8877` in this template app).

In order to test if the application is up, you can call its health endpoint:

```bash
  curl http://localhost:8877/health
```

You should get a response similar to this:

```
  {"status":"UP","diskSpace":{"status":"UP","total":249644974080,"free":137188298752,"threshold":10485760}}
```

### Alternative script to run application

To skip all the setting up and building, just execute the following command:

```bash
./bin/run-in-docker.sh
```

For more information:

```bash
./bin/run-in-docker.sh -h
```

Script includes bare minimum environment variables necessary to start api instance. Whenever any variable is changed or any other script regarding docker image/container build, the suggested way to ensure all is cleaned up properly is by this command:

```bash
docker-compose rm
```

It clears stopped containers correctly. Might consider removing clutter of images too, especially the ones fiddled with:

```bash
docker images

docker image rm <image-id>
```

There is no need to remove postgres and java or similar core images.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details

