package com.zenvia.komposer.runner

import com.spotify.docker.client.LogStream
import com.spotify.docker.client.messages.ContainerCreation
import com.spotify.docker.client.messages.ContainerInfo
import de.gesellix.docker.client.DockerClient
import spock.lang.Specification

/**
 * @author Tiago de Oliveira
 * */
class KomposerRunnerSpec extends Specification {

    DockerClient dockerClient = Mock(DockerClient)
    def runner = new KomposerRunner(dockerClient)
    def services = ['sender': [containerId: '9998877', containerName: 'komposer_resources_sender_']]

    def "Up"() {
        given:
            def file = 'src/test/resources/docker-compose.yml'
            def creation = new ContainerCreation()
            creation.id = '9998877'
            def info = new ContainerInfo()
            def stream = Mock(LogStream)
        when:
            dockerClient.createContainer(_, _) >> creation
            dockerClient.inspectContainer(creation.id) >> info
            dockerClient.logs(_, _) >> stream
            def result = runner.up(file)
        then:
            result
            result.sender.containerId == services.sender.containerId
            result.sender.containerName.contains(services.sender.containerName)
            result.sender.containerInfo == info
    }

    def "Down"() {
        when:
            runner.down(services)
        then:
            dockerClient.stop(_) >> { argument ->
                assert argument[0] == services.sender.containerId
            }
    }

    def "Rm"() {
        when:
            runner.rm(services)
        then:
            dockerClient.rm(_) >> { argument ->
                assert argument[0] == services.sender.containerId
            }
    }

    def "Stop"() {
        when:
            runner.stop(services.sender.containerId)
        then:
            dockerClient.stop(_) >> { argument ->
                assert argument[0] == services.sender.containerId
            }
    }

    def "Start"() {
        when:
            runner.start(services.sender.containerId)
        then:
            dockerClient.startContainer(_) >> { argument ->
                assert argument[0] == services.sender.containerId
            }
    }

    def "Finish"() {
        when:
            runner.finish()
        then:
            assert runner.dockerClient == null
    }
}
