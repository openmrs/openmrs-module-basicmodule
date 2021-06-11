### Important links for reference:
- [OPENMRS WIKI](https://wiki.openmrs.org/)
- [FHIR](https://hl7.org/FHIR/documentation.html)
- [SBX healthID creation](https://healthidsbx.ndhm.gov.in/)
- [HIU](https://dev.ndhm.gov.in/hiu#/)
- [PHR/NDHM Architecture](https://sandbox.ndhm.gov.in/docs/building_blocks)
- [HIP API Standards](https://sandbox.ndhm.gov.in/swagger/ndhm-hip.yaml)


## Services:
Clone the basic 5 services:
- [OMOD-HIP](https://github.com/Bahmni-Covid19/openmrs-module-hip.git)
- [Hip Service](https://github.com/Bahmni-Covid19/hip-service)
- [BahmniApps](https://github.com/Bahmni-Covid19/openmrs-module-bahmniapps)
- [Ndhm-React](https://github.com/Bahmni-Covid19/ndhm-react)
- [Default-config](https://github.com/Bahmni-Covid19/default-config)

Others can be found here : [Bahmni-Covid19](https://github.com/Bahmni-Covid19/)

### Setup Bahmni using Vagrant:

1. Install [Virtualbox](https://www.virtualbox.org/wiki/Downloads)
2. Install [Vagrant](https://www.vagrantup.com/downloads.html) 
3. Clone the `bahmni-vagrant` repo from github inside a folder called `bahmni`

   ```
   mkdir bahmni
   cd bahmni
   git clone https://github.com/Bahmni/bahmni-vagrant.git
   cd bahmni-vagrant
   ```

4. Replace the contents of the `Vagrantfile` in `bahmni-vagrant` with the following:
    
   ```
    Vagrant.configure(2) do |config|
      config.vm.box = "bento/centos-7.6"
      config.vm.box_check_update = true
      config.ssh.insert_key = false
      config.vm.network "private_network", ip: "192.168.33.10"
      config.vm.network "forwarded_port", guest: 3306, host: 3306
      config.vm.network "forwarded_port", guest: 8000, host: 8000
      config.vm.network "forwarded_port", guest: 80, host: 80
    
      config.vm.synced_folder "..", "/bahmni", :owner => "vagrant"
      config.vm.provider "virtualbox" do |v|
         v.customize ["modifyvm", :id, "--memory", 3092, "--cpus", 2, "--name", "Bahmni-RPM"]
      end
    end
   ```
   
5. Make vagrant up and open vagrant
    
   ```
    vagrant up && vagrant ssh
   ```

6. Now, inside the vagrant box set up a local Bahmni instance by using this document
(Note: install the version 0.92 provided in the [link](https://bahmni.atlassian.net/wiki/spaces/BAH/pages/33128505/Install+Bahmni+on+CentOS))

7. Open [bahmni-emr-login](https://192.168.33.10/bahmni/home/index.html#/login), you should see bahmni up and running.  
    - Username: superman
    - Password: Admin123

### Prerequisites for OMOD-HIP:
   
- Download following jars and fhir
    - [jackson-core-2.10.0.jar](https://repo1.maven.org/maven2/com/fasterxml/jackson/core/jackson-core/2.10.0/jackson-core-2.10.0.jar)
    - [jackson-annotations-2.10.0.jar](https://repo1.maven.org/maven2/com/fasterxml/jackson/core/jackson-annotations/2.10.0/jackson-annotations-2.10.0.jar)
    - [jackson-databind-2.10.0.jar](https://repo1.maven.org/maven2/com/fasterxml/jackson/core/jackson-databind/2.10.0/jackson-databind-2.10.0.jar)
    - [fhir2-omod-1.0.0-20200925.143534-163.jar](https://openmrs.jfrog.io/artifactory/public/org/openmrs/module/fhir2-omod/1.0.0-SNAPSHOT/fhir2-omod-1.0.0-20200925.143534-163.jar)
- Rename `fhir2-omod-1.0.0-20200925.143534-163.jar` file from `.jar` to `.omod` extension.
- Copy all fies to `bahmni` folder
- Open vagrant and paste the files 

  ```
  cd bahmni-vagrant
  vagrant ssh
  cp /bahmni/jackson-core-2.10.0.jar /opt/openmrs/openmrs/WEB-INF/lib/
  cp /bahmni/jackson-annotations-2.10.0.jar /opt/openmrs/openmrs/WEB-INF/lib/
  cp /bahmni/jackson-databind-2.10.0.jar /opt/openmrs/openmrs/WEB-INF/lib/
  cp /bahmni/fhir2-omod-1.0.0-20200925.143534-163.omod /opt/openmrs/modules/
  ```  
- Restart openmrs `systemctl restart openmrs`

- Ensure that the module is successfully loaded by navigating through [openmrs](http://192.168.33.10/openmrs)
    - username: superman
    - password: Admin123
    - Click on `Administration` -> `Manage Modules` under `Modules` 
- You can also check the log file for any errors/exceptions: `vi /opt/openmrs/openmrs.log`
 in vagrant. 
 
### Setting up the OMOD-HIP

(Make sure `bahmni` folder is created and `bahmni vagrant` is setup inside `bahmni`)
1. Clone OMOD-HIP

    ```
    cd bahmni
    git clone https://github.com/Bahmni-Covid19/openmrs-module-hip.git
   ```

2. Run the steps to build .omod file

    ```
    cd openmrs-module-hip
    mvn clean install
    ```

3. Move the newly build omod to vagrant and overwrite  it and restart OMOD with the new omod

    ```
    vagrant ssh
    sudo su
    cp /bahmni/openmrs-module-hip/omod/target/hipmodule-omod-0.1-SNAPSHOT.omod /opt/openmrs/modules/
    systemctl restart openmrs
    ```

4. (Optional) Check log file to see if any errors occurred inside vagrant

    ```
    vi /opt/openmrs/openmrs.log

    ```
### Installations:

Please make sure following are done before proceeding forward
- [Yarn](https://classic.yarnpkg.com/en/)
- [Node.js(v10.11.0)](https://classic.yarnpkg.com/en/)
- [Maven](https://maven.apache.org/)
- [Ruby v2.1 (or above)](https://www.ruby-lang.org/en/documentation/installation/)
- [Compass](http://compass-style.org/install/) to compile the SCSS files.
- Install Firefox to run tests for `bahmni-apps`

### Setting up Bahmni-Apps

1. Clone bahmni-apps
    
   ```
   cd bahmni
   git clone https://github.com/Bahmni-Covid19/openmrs-module-bahmniapps.git
   cd openmrs-module-bahmniapps
    ```
   
2. Change branch to stream1/master (which is the current working master for Hip stream)
   
    ```
    git checkout stream1/master
    ```
   
3. Run following to build the project 
    ```
   cd /ui
   yarn install
   yarn default
    ```
### Setting up ndhm-react (verify-btn pop-up)

1. Clone ndhm-react
    
   ```
   cd bahmni
   git clone https://github.com/Bahmni-Covid19/ndhm-react.git
   cd ndhm-react
    ```

2. In master branch run following to build project 

    ```
    yarn install
    yarn build
    ```

### Setting up default-config

1. Clone default-config
    
   ```
   cd bahmni
   git clone https://github.com/Bahmni-Covid19/default-config.git
   cd default-config
    ```
2. Change branch to stream1/master (which is the current working master for Hip stream)
   
    ```
    git checkout stream1/master
    ```
3. Browse to this file `openmrs/apps/registration/extension.json` and change host to localhost in `NDHMIdentifierLookup`,`extensionParams`

    ```
    "hipUrl" : "http://localhost:9052",
    "bahmniUrl": "https://192.168.33.10/openmrs/ws/rest/v1/hip"
    ```

## Linking the repos inside your local vagrant

1. Go inside local vagrant
 
    ```
   cd bahmni-vagrant
   vagrant ssh
   cd /var/www/
   ll
    ```
   you will see bahmniapps and bahmni_config being linked already to older versions, we will unlink and link to our local repos
2. Unlink old links

    ```
   unlink bahmniapps
   unlink bahmni_config
    ```
     
3. Link new build repositories
    
    ```
   link -s /bahmni/ndhm-react/build ndhm
   link -s /bahmni/default-config bahmni_config
   link -s /bahmni/openmrs-module-bahmniapps/ui/app bahmniapps 
   ```

4. Change config setting for `ndhm` redirection

    ```
   vi /etc/httpd/conf.d/ssl.conf
    ```
   
5. Search for Alias by typing `/Alias` and after this line `Alias /implementer-interface /var/www/implementer_interface` add 
    
   ```
    Alias /ndhm /var/www/ndhm
    ```