package alemiz.stargate.docker;

import alemiz.stargate.StarGate;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.Ports;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;

import java.util.ArrayList;
import java.util.List;

public class DockerConnection {

    private StarGate plugin;

    private String address = "0.0.0.0";
    private String port = "2375";


    private DockerClient client;

    public DockerConnection(String hostName, String port){
        this.address = hostName;
        this.port = port;

        this.plugin = StarGate.getInstance();
    }

    public DockerConnection connect(){
        DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
                .withDockerHost("tcp://"+this.address+":"+this.port)
                .build();

        this.client = DockerClientBuilder.getInstance(config).build();
        return this;
    }

    public String createContainer(String image, String[] exposedPorts, String[] envVariables){
        Ports portBindings = new Ports();
        List<ExposedPort> exposedList = new ArrayList<>();

        for (String serialized : exposedPorts){
            String[] parts = serialized.split("/");

            try {
                ExposedPort exposedPortTcp = ExposedPort.tcp(Integer.parseInt(parts[0]));
                ExposedPort exposedPortUdp = ExposedPort.udp(Integer.parseInt(parts[0]));

                Ports.Binding binding = Ports.Binding.bindPort(Integer.parseInt((parts.length == 1? parts[0] : parts[1])));

                portBindings.bind(exposedPortTcp, binding);
                portBindings.bind(exposedPortUdp, binding);

                exposedList.add(exposedPortTcp);
                exposedList.add(exposedPortUdp);
            }catch (Exception e){
                this.plugin.getLogger().warning("§eERROR: Bad parsed port while creating container! Port: "+serialized);
                return null;
            }
        }

        HostConfig hostConfig = HostConfig.newHostConfig()
                .withPortBindings(portBindings);

        CreateContainerResponse container = this.client.createContainerCmd(image)
                .withTty(true)
                .withStdinOpen(true)
                .withExposedPorts(exposedList)
                .withHostConfig(hostConfig)
                .withEnv(envVariables)
                .exec();

        this.startContainer(container.getId());
        return container.getId();
    }

    public void removeContainer(String id, boolean removeVolumes){
        this.client.removeContainerCmd(id)
                .withRemoveVolumes(removeVolumes)
                .exec();
    }

    public void startContainer(String id){
        this.client.startContainerCmd(id).exec();
    }

    public void stopContainer(String id){
        this.client.stopContainerCmd(id).exec();
    }

    public DockerClient getClient() {
        return client;
    }
}
