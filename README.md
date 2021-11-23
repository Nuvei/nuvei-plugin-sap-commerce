# Installation and Usage

## Installing the Nuvei Connector into your existent SAP Commerce installation

First ensure that the version of SAP Commerce Cloud being used is supported for the plugin. Please view the Compatibility section for the current list of supported versions.

The connector contains several SAP Commerce Cloud extensions. Take the following steps to include the plugin into your SAP Commerce Cloud application:

1. Unzip the supplied plugin zip file

1. Copy the extracted folders to the ${HYBRIS_BIN_DIR} of your SAP Commerce installation.

1. Run the ‘ant clean’ command from within your hybris bin/platform directory.

1. Copy the following lines into your localextensions.xml after <path dir="${HYBRIS_BIN_DIR}"/>. The extensions do not rely on any absolute paths, so it is also possible to place the extensions in a different location (such as ${HYBRIS_BIN_DIR}/custom).
  
     ```
      <path autoload="false" dir="${HYBRIS_BIN_DIR}/modules/nuvei"/>
      
      <extension name='nuveiaddon' /> 
      <extension name='nuveiservices' />
      <extension name='nuveifacades' /> 
      <extension name='nuveibackoffice' /> 
      <extension name='nuveisampledataaddon' /> 
      <extension name='nuveinotifications'  /> 
      <extension name='nuveifulfilment'  /> 
     
    ```
1. Run the commands below to install specific add-ons of the yaccelatorstorefront (replace "yacceleratorstorefront" with your custom storefront if relevant). The generic installations instructions are:

    ```
    ant addoninstall -Daddonnames="nuveiaddon" -DaddonStorefront.yacceleratorstorefront="yacceleratorstorefront"
    ```

1. Optional: The nuveisampledataaddon is optional, and can be installed by running the command:
   ```
   ant addoninstall -Daddonnames="nuveisampledataaddon" -DaddonStorefront.yacceleratorstorefront="yacceleratorstorefront"
   ```
   
1. Run the `ant clean all` command from within your bin/platform directory.

1. Update your running system using `ant updatesystem`

1. Run `hybrisserver.sh` to startup the SAP Commerce server.


## Installing the Plugin using the provided recipes

The Connector ships with a gradle recipe to be used with the SAP Commerce installer:

B2C: b2c_acc_plus_nuvei with b2c and Nuvei functionality.

To use recipes on a clean installation:

1. Unzip the supplied plugin zip file

1. Copy the extracted folders to the root folder of your SAP Commerce installation.

1. Run the following commands:

Create a solution from the accelerator templates and install the addons.
```
HYBRIS_HOME/installer$ ./install.sh -r b2c_acc_plus_nuvei setup -A local_property:initialpassword.admin=nimda
```

Build and initialize the platform
```
HYBRIS_HOME/installer$ ./install.sh -r b2c_acc_plus_nuvei initialize -A local_property:initialpassword.admin=nimda
```

Start a commerce suite instance
```
HYBRIS_HOME/installer$ ./install.sh -r b2c_acc_plus_nuvei start -A local_property:initialpassword.admin=nimda
```

## Installing the Plugin using in CCV2

A manifest file has been provided as example

Update the manifest with the following content.

1. Add the nuvei extensions under the nuveiaddon section
    ```json
     "extensions": [
     ...
     ...
     "nuveiaddon", 
     "nuveiservices",
     "nuveifacades",
     "nuveibackoffice",
     "nuveisampledataaddon",
     "nuveinotifications",
     "nuveifulfilment",
     ...
     ...
    ],
    ```
    
1. Add the nuveiaddon under the storefrontAddons section, replacing the storefront value yacceleratorstorefront with the name of your storefront (only the storefront. The template value should remain as yacceleratorstorefront) :

    ```json
      "storefrontAddons": [
      ...
      ...
      {
        "addon": "nuveiaddon",
        "storefront": "yacceleratorstorefront",
        "template": "yacceleratorstorefront"
      },
    ```

1. To be able to receive the notifications, create a webapp under the accstorefront aspect:

    ```json
     "aspects": [
     ...
     ...
      {
          "name": "accstorefront",
          "webapps": [
              ...
              ...
              {
                "name": "nuveinotifications",
                "contextPath": "/nuveinotifications"
              },
              ...
              ...
          ]
          ...
          ...
        }
      ]
    ```
# License
This repository is open source and available under the MIT license.

Copyright (c) 2021 Nuvei

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
