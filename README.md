[![Dependency Status](https://www.versioneye.com/user/projects/5723677aba37ce00464e0496/badge.svg?style=flat)](https://www.versioneye.com/user/projects/5723677aba37ce00464e0496)

# h2o-broker
Cloud foundry broker for h2o. It spawns h2o instances on yarn.


# How to use it?
To use h2o-broker, you need to build it from sources configure, deploy, create instance and bind it to your app to get h2o credentials. Instead of binding h2o manually you can use [service-exposer](https://github.com/trustedanalytics/service-exposer). Follow steps described below.


## Build
Run command for compile and package:
```
mvn clean package
```


## Kerberos configuration
Broker automatically binds to an existing kerberos service. This will provide default kerberos configuration, for REALM and KDC host. Before deploy check:

- if kerberos service does not exists in your space, you can create it with command:
```
cf cups kerberos-service -p '{ "kdc": "kdc-host", "kpassword": "kerberos-password", "krealm": "kerberos-realm", "kuser": "kerberos-user" }'
```

- if kerberos-service exists in your space, you can update it with command:
```
cf uups kerberos-service -p '{ "kdc": "kdc-host", "kpassword": "kerberos-password", "krealm": "kerberos-realm", "kuser": "kerberos-user" }'
```


## H2O provisioner configuration
Broker automatically binds to an existing h2o-provisioner service. Before deploy check:

- if h2o-provisioner service does not exists in your space, you can create it with command:
```
cf cups h2o-provisioner -p '{"url":"<url-placeholder>:<port-placeholder>"}'
```

- if h2o-provisioner exists in your space, you can update it with command:
```
cf uups h2o-provisioner -p '{"url":"<url-placeholder>:<port-placeholder>"}'
```


## Configure
For strict separation of config from code (twelve-factor principle), configuration must be placed in environment variables.
During build of h2o-broker manifest.yml file should be generated. There will be couple of "placeholder" fields that you should fill.

Broker configuration params list (environment properties):
* obligatory :
  * USER_PASSWORD - password to interact with service broker
  * BASE_GUID - base id for catalog plan creation (uuid)
  * HADOOP_PROVIDED_PARAMS - list of yarn configuration parameters, it has to be proper json form:
      ```json
          {"HADOOP_CONFIG_KEY":
              {
                  "property1.name":"property1.value",
                  "property2.name":"property2.value",
                  ...
              }
          }

      ```
      **Remember to put json in 'single quotes' and to escape all $ characters.**

      You can use **import_hadoop_conf.sh** script available in admin tool kit https://github.com/trustedanalytics/hadoop-admin-tools. There are several ways to use of this util:

      Getting hadoop configuration directly from CDH manager.
      ```
      ./import_hadoop_conf.sh -cu http://<cloudera_manager_host_name>:7180/cmf/services/5/client-config
      ```

      Getting hadoop configuration from local archive.
      ```
      ./import_hadoop_conf.sh -cu file://path/client-config.zip
      ```

      Getting hadoop configuration from stdin.
      ```
      cat /path/client-config.zip | ./import_hadoop_conf.sh
      ```
* optional :
  * MAPPER_NODES - number of h2o nodes to be spawned
  * MAPPER_MEMORY - amount of memory for every h2o node (examples: 256m, 512m, 1g)
  * CF_CATALOG_SERVICENAME - service name in cloud foundry catalog (default: h2o)
  * CF_CATALOG_SERVICEID - service id in cloud foundry catalog (default: h2o)
  * IMAGE_URL - base64 img with service icon


## Deploy
Push broker binary code to cloud foundry (use cf client).:
```
cf push
```


## Create new service instance

Use cf client :
```
cf create-service-broker h2o-broker <user> <password> https://h2o-broker.<platform_domain>
cf enable-service-access h2o
cf cs h2o shared h2o-instance
```

## Binding broker instance

Broker instance can be bind with cf client :
```
cf bs <app> h2o-instance
```

To check if broker instance is bound, use cf client :
```
cf env <app>
```
and look for :
```yaml
  "h2o": [
     {
      "credentials": {
       "hostname": "10.10.10.117",
       "password": "MGImIG4QBlNg",
       "port": "54323",
       "username": "b20qy4sm"
      },
      "label": "h2o",
      "name": "h2o-instance",
      "plan": "shared",
      "tags": []
     }
    ]
```
in VCAP_SERVICES.

## Useful links

Cloud foundry resources that are helpful when troubleshooting service brokers :
 * http://docs.cloudfoundry.org/services/api.html
 * http://docs.cloudfoundry.org/devguide/services/managing-services.html#update_service
 * http://docs.cloudfoundry.org/services/access-control.html

