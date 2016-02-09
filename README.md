# echo-server
Sample java application for Cisco IOx as part of developer tutorials

Primarily intended to demonstrate:

1) IOx Packaging descriptor
2) Specifying Resources and network needed on IOx devices

### Build 

mvn clean install

### Run the app locally

Once the java application is successfully built, go to the target/distro folder and run the below command

    java -cp "./*;classes/;lib/*" -Djava.util.logging.config.file=logging.properties sample.echoserver.EchoServer
    
After successfully running the application, verify the functionality of the echo server by running the below command.

echo 'Hello!' | curl -d @- http://localhost:8080
Response of the above command shall be echoing back the Hello message.

### Run on IOx device

Follow Cisoco IOx documentation tp apckage and deploy the application on IOx Device  
