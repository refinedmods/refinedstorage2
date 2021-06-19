package com.refinedmods.refinedstorage2.core.network.host;

import com.refinedmods.refinedstorage2.core.Rs2Test;
import com.refinedmods.refinedstorage2.core.adapter.FakeRs2World;
import com.refinedmods.refinedstorage2.core.network.Network;
import com.refinedmods.refinedstorage2.core.network.NetworkUtil;
import com.refinedmods.refinedstorage2.core.network.component.GraphNetworkComponent;
import com.refinedmods.refinedstorage2.core.util.Position;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Rs2Test
class NetworkNodeHostImplTest {
    @Test
    void Test_forming_new_network() {
        // Arrange
        FakeRs2World world = new FakeRs2World();

        NetworkNodeHost<?> host = NetworkUtil.createHost(world, Position.ORIGIN);
        NetworkNodeHost<?> unrelatedHost = NetworkUtil.createHostWithNetwork(world, Position.ORIGIN.down().down());

        FakeNetworkNodeHostRepository repository = FakeNetworkNodeHostRepository.of(host, unrelatedHost);

        // Act
        host.initialize(repository, NetworkUtil.NETWORK_COMPONENT_REGISTRY);

        // Assert
        assertThat(host.getNode().getNetwork()).isNotNull();
        assertThat(host.getNode().getNetwork().getComponent(GraphNetworkComponent.class).getHosts()).containsExactly(NetworkNodeHostEntry.create(host));

        assertThat(NetworkUtil.getAddedHosts(host.getNode().getNetwork())).containsExactly(host);
        assertThat(NetworkUtil.getRemovedHosts(host.getNode().getNetwork())).isEmpty();
        assertThat(NetworkUtil.getNetworkRemovedCount(host.getNode().getNetwork())).isZero();
        assertThat(NetworkUtil.getNetworkMerges(host.getNode().getNetwork())).isEmpty();

        assertThat(unrelatedHost.getNode().getNetwork()).isNotNull().isNotEqualTo(host.getNode().getNetwork());
        assertThat(unrelatedHost.getNode().getNetwork().getComponent(GraphNetworkComponent.class).getHosts()).containsExactly(NetworkNodeHostEntry.create(unrelatedHost));
    }

    @Test
    void Test_joining_existing_network() {
        // Arrange
        FakeRs2World world = new FakeRs2World();

        NetworkNodeHost<?> existingHost1 = NetworkUtil.createHostWithNetwork(world, Position.ORIGIN);
        NetworkNodeHost<?> existingHost2 = NetworkUtil.createHostWithNetwork(world, Position.ORIGIN.up(), host -> existingHost1.getNode().getNetwork());
        NetworkNodeHost<?> newHost = NetworkUtil.createHost(world, Position.ORIGIN.down());
        NetworkNodeHost<?> unrelatedHost = NetworkUtil.createHostWithNetwork(world, Position.ORIGIN.down().down().down());

        FakeNetworkNodeHostRepository repository = FakeNetworkNodeHostRepository.of(existingHost1, existingHost2, newHost, unrelatedHost);

        // Act
        newHost.initialize(repository, NetworkUtil.NETWORK_COMPONENT_REGISTRY);

        // Assert
        Network expectedNetwork = existingHost1.getNode().getNetwork();

        assertThat(existingHost1.getNode().getNetwork()).isEqualTo(expectedNetwork);
        assertThat(existingHost2.getNode().getNetwork()).isEqualTo(expectedNetwork);
        assertThat(newHost.getNode().getNetwork()).isEqualTo(expectedNetwork);
        assertThat(expectedNetwork.getComponent(GraphNetworkComponent.class).getHosts()).containsExactlyInAnyOrder(
                NetworkNodeHostEntry.create(existingHost1),
                NetworkNodeHostEntry.create(existingHost2),
                NetworkNodeHostEntry.create(newHost)
        );

        assertThat(NetworkUtil.getAddedHosts(expectedNetwork)).containsExactly(existingHost1, existingHost2, newHost);
        assertThat(NetworkUtil.getRemovedHosts(expectedNetwork)).isEmpty();
        assertThat(NetworkUtil.getNetworkRemovedCount(expectedNetwork)).isZero();
        assertThat(NetworkUtil.getNetworkMerges(expectedNetwork)).isEmpty();

        assertThat(unrelatedHost.getNode().getNetwork()).isNotNull().isNotEqualTo(expectedNetwork);
        assertThat(unrelatedHost.getNode().getNetwork().getComponent(GraphNetworkComponent.class).getHosts()).containsExactly(NetworkNodeHostEntry.create(unrelatedHost));
    }

    @Test
    void Test_merging_with_existing_networks() {
        // Arrange
        FakeRs2World world = new FakeRs2World();

        NetworkNodeHost<?> existingHost0 = NetworkUtil.createHostWithNetwork(world, Position.ORIGIN.up());
        NetworkNodeHost<?> existingHost1 = NetworkUtil.createHostWithNetwork(world, Position.ORIGIN, host -> existingHost0.getNode().getNetwork());
        NetworkNodeHost<?> existingHost2 = NetworkUtil.createHostWithNetwork(world, Position.ORIGIN.down().down());
        Network initialNetworkOfExistingHost1 = existingHost1.getNode().getNetwork();
        NetworkNodeHost<?> unrelatedHost = NetworkUtil.createHostWithNetwork(world, Position.ORIGIN.east().east());
        NetworkNodeHost<?> newHost = NetworkUtil.createHost(world, Position.ORIGIN.down());

        FakeNetworkNodeHostRepository repository = FakeNetworkNodeHostRepository.of(existingHost1, existingHost2, existingHost0, newHost, unrelatedHost);

        // Act
        newHost.initialize(repository, NetworkUtil.NETWORK_COMPONENT_REGISTRY);

        // Assert
        Network expectedNetwork = existingHost2.getNode().getNetwork();

        assertThat(NetworkUtil.getNetworkMerges(initialNetworkOfExistingHost1)).containsExactlyInAnyOrder(expectedNetwork);

        assertThat(existingHost1.getNode().getNetwork()).isEqualTo(expectedNetwork);
        assertThat(existingHost2.getNode().getNetwork()).isEqualTo(expectedNetwork);
        assertThat(existingHost0.getNode().getNetwork()).isEqualTo(expectedNetwork);
        assertThat(newHost.getNode().getNetwork()).isEqualTo(expectedNetwork);
        assertThat(expectedNetwork.getComponent(GraphNetworkComponent.class).getHosts()).containsExactlyInAnyOrder(
                NetworkNodeHostEntry.create(existingHost1),
                NetworkNodeHostEntry.create(existingHost2),
                NetworkNodeHostEntry.create(existingHost0),
                NetworkNodeHostEntry.create(newHost)
        );

        assertThat(NetworkUtil.getAddedHosts(expectedNetwork)).containsExactlyInAnyOrder(existingHost2, newHost, existingHost1, existingHost0);
        assertThat(NetworkUtil.getRemovedHosts(expectedNetwork)).isEmpty();
        assertThat(NetworkUtil.getNetworkRemovedCount(expectedNetwork)).isZero();
        assertThat(NetworkUtil.getNetworkMerges(expectedNetwork)).isEmpty();

        assertThat(unrelatedHost.getNode().getNetwork()).isNotNull().isNotEqualTo(expectedNetwork);
        assertThat(unrelatedHost.getNode().getNetwork().getComponent(GraphNetworkComponent.class).getHosts()).containsExactly(NetworkNodeHostEntry.create(unrelatedHost));
    }

    @Test
    void Test_should_form_network_if_there_are_neighbors_with_no_network() {
        // Arrange
        FakeRs2World world = new FakeRs2World();

        NetworkNodeHost<?> unrelatedHost = NetworkUtil.createHostWithNetwork(world, Position.ORIGIN.east().east());

        NetworkNodeHost<?> host1 = NetworkUtil.createHost(world, Position.ORIGIN);
        NetworkNodeHost<?> host2 = NetworkUtil.createHost(world, Position.ORIGIN.down());
        NetworkNodeHost<?> host3 = NetworkUtil.createHost(world, Position.ORIGIN.down().down());

        FakeNetworkNodeHostRepository repository = FakeNetworkNodeHostRepository.of(host1, host2, host3, unrelatedHost);

        // Act
        host1.initialize(repository, NetworkUtil.NETWORK_COMPONENT_REGISTRY);
        host2.initialize(repository, NetworkUtil.NETWORK_COMPONENT_REGISTRY);
        host3.initialize(repository, NetworkUtil.NETWORK_COMPONENT_REGISTRY);

        // Assert
        Network expectedNetwork = host1.getNode().getNetwork();

        assertThat(host1.getNode().getNetwork()).isEqualTo(expectedNetwork);
        assertThat(host2.getNode().getNetwork()).isEqualTo(expectedNetwork);
        assertThat(host3.getNode().getNetwork()).isEqualTo(expectedNetwork);
        assertThat(expectedNetwork.getComponent(GraphNetworkComponent.class).getHosts()).containsExactlyInAnyOrder(
                NetworkNodeHostEntry.create(host1),
                NetworkNodeHostEntry.create(host2),
                NetworkNodeHostEntry.create(host3)
        );

        assertThat(NetworkUtil.getNetworkMerges(expectedNetwork)).isEmpty();
        assertThat(NetworkUtil.getAddedHosts(expectedNetwork)).containsExactlyInAnyOrder(host1, host2, host3);
        assertThat(NetworkUtil.getRemovedHosts(expectedNetwork)).isEmpty();
        assertThat(NetworkUtil.getNetworkRemovedCount(expectedNetwork)).isZero();

        assertThat(unrelatedHost.getNode().getNetwork()).isNotNull().isNotEqualTo(expectedNetwork);
        assertThat(unrelatedHost.getNode().getNetwork().getComponent(GraphNetworkComponent.class).getHosts()).containsExactly(NetworkNodeHostEntry.create(unrelatedHost));
    }

    @Test
    void Test_should_split_network() {
        // Arrange
        FakeRs2World world = new FakeRs2World();

        NetworkNodeHost<?> host1 = NetworkUtil.createHostWithNetwork(world, Position.ORIGIN);
        NetworkNodeHost<?> host2 = NetworkUtil.createHostWithNetwork(world, Position.ORIGIN.down(), host -> host1.getNode().getNetwork());
        NetworkNodeHost<?> host3 = NetworkUtil.createHostWithNetwork(world, Position.ORIGIN.down().down(), host -> host1.getNode().getNetwork());

        NetworkNodeHost<?> unrelatedHost = NetworkUtil.createHostWithNetwork(world, Position.ORIGIN.east().east());

        FakeNetworkNodeHostRepository repository = FakeNetworkNodeHostRepository.of(host1, host2, host3, unrelatedHost);

        // Act
        repository.removeHost(world, host1.getPosition());
        host1.remove(repository, NetworkUtil.NETWORK_COMPONENT_REGISTRY);

        // Assert
        assertThat(host1.getNode().getNetwork()).isNull();

        assertThat(host2.getNode().getNetwork())
                .isSameAs(host3.getNode().getNetwork())
                .isNotSameAs(unrelatedHost.getNode().getNetwork());

        assertThat(host2.getNode().getNetwork().getComponent(GraphNetworkComponent.class).getHosts()).containsExactlyInAnyOrder(
                NetworkNodeHostEntry.create(host2),
                NetworkNodeHostEntry.create(host3)
        );

        assertThat(NetworkUtil.getNetworkSplits(host2.getNode().getNetwork())).isEmpty();
        assertThat(NetworkUtil.getAddedHosts(host2.getNode().getNetwork())).containsExactly(host1, host2, host3);
        assertThat(NetworkUtil.getRemovedHosts(host2.getNode().getNetwork())).containsExactly(host1);
        assertThat(NetworkUtil.getNetworkRemovedCount(host2.getNode().getNetwork())).isZero();
    }

    @Test
    void Test_should_split_network_in_two() {
        // Arrange
        FakeRs2World world = new FakeRs2World();

        NetworkNodeHost<?> host1 = NetworkUtil.createHostWithNetwork(world, Position.ORIGIN);
        NetworkNodeHost<?> host2 = NetworkUtil.createHostWithNetwork(world, Position.ORIGIN.down(), host -> host1.getNode().getNetwork());

        NetworkNodeHost<?> host3 = NetworkUtil.createHostWithNetwork(world, Position.ORIGIN.down().down(), host -> host1.getNode().getNetwork());

        NetworkNodeHost<?> host4 = NetworkUtil.createHostWithNetwork(world, Position.ORIGIN.down().down().down(), host -> host1.getNode().getNetwork());
        NetworkNodeHost<?> host5 = NetworkUtil.createHostWithNetwork(world, Position.ORIGIN.down().down().down().down(), host -> host1.getNode().getNetwork());

        NetworkNodeHost<?> unrelatedHost = NetworkUtil.createHostWithNetwork(world, Position.ORIGIN.east().east());

        FakeNetworkNodeHostRepository repository = FakeNetworkNodeHostRepository.of(host1, host2, host3, host4, host5, unrelatedHost);

        // Act
        repository.removeHost(world, host3.getPosition());
        host3.remove(repository, NetworkUtil.NETWORK_COMPONENT_REGISTRY);

        // Assert
        assertThat(host3.getNode().getNetwork()).isNull();

        assertThat(host1.getNode().getNetwork())
                .isSameAs(host2.getNode().getNetwork())
                .isNotSameAs(unrelatedHost.getNode().getNetwork())
                .isNotSameAs(host4.getNode().getNetwork())
                .isNotSameAs(host5.getNode().getNetwork());

        assertThat(NetworkUtil.getNetworkSplits(host1.getNode().getNetwork())).isEmpty();
        assertThat(NetworkUtil.getAddedHosts(host1.getNode().getNetwork())).containsExactlyInAnyOrder(host1, host2);
        assertThat(NetworkUtil.getRemovedHosts(host1.getNode().getNetwork())).isEmpty();
        assertThat(NetworkUtil.getNetworkRemovedCount(host1.getNode().getNetwork())).isZero();

        assertThat(host1.getNode().getNetwork().getComponent(GraphNetworkComponent.class).getHosts()).containsExactlyInAnyOrder(
                NetworkNodeHostEntry.create(host1),
                NetworkNodeHostEntry.create(host2)
        );

        assertThat(host4.getNode().getNetwork())
                .isSameAs(host5.getNode().getNetwork())
                .isNotSameAs(unrelatedHost.getNode().getNetwork())
                .isNotSameAs(host1.getNode().getNetwork())
                .isNotSameAs(host2.getNode().getNetwork());

        List<Set<Network>> splits = NetworkUtil.getNetworkSplits(host4.getNode().getNetwork());
        assertThat(splits).hasSize(1);
        assertThat(splits.get(0)).containsExactlyInAnyOrder(host1.getNode().getNetwork());

        assertThat(NetworkUtil.getAddedHosts(host4.getNode().getNetwork())).containsExactlyInAnyOrder(host1, host2, host3, host4, host5);
        assertThat(NetworkUtil.getRemovedHosts(host4.getNode().getNetwork())).containsExactlyInAnyOrder(host1, host2, host3);
        assertThat(NetworkUtil.getNetworkRemovedCount(host4.getNode().getNetwork())).isZero();

        assertThat(host4.getNode().getNetwork().getComponent(GraphNetworkComponent.class).getHosts()).containsExactlyInAnyOrder(
                NetworkNodeHostEntry.create(host4),
                NetworkNodeHostEntry.create(host5)
        );
    }

    @Test
    void Test_should_split_network_in_three() {
        // Arrange
        FakeRs2World world = new FakeRs2World();

        NetworkNodeHost<?> host1 = NetworkUtil.createHostWithNetwork(world, Position.ORIGIN);

        NetworkNodeHost<?> host2 = NetworkUtil.createHostWithNetwork(world, Position.ORIGIN.up(), host -> host1.getNode().getNetwork());

        NetworkNodeHost<?> host3 = NetworkUtil.createHostWithNetwork(world, Position.ORIGIN.down(), host -> host1.getNode().getNetwork());

        NetworkNodeHost<?> host4 = NetworkUtil.createHostWithNetwork(world, Position.ORIGIN.north(), host -> host1.getNode().getNetwork());
        NetworkNodeHost<?> host5 = NetworkUtil.createHostWithNetwork(world, Position.ORIGIN.north().north(), host -> host1.getNode().getNetwork());

        NetworkNodeHost<?> unrelatedHost = NetworkUtil.createHostWithNetwork(world, Position.ORIGIN.south().south());

        FakeNetworkNodeHostRepository repository = FakeNetworkNodeHostRepository.of(host1, host2, host3, host4, host5, unrelatedHost);

        // Act
        repository.removeHost(world, host1.getPosition());
        host1.remove(repository, NetworkUtil.NETWORK_COMPONENT_REGISTRY);

        // Assert
        assertThat(host1.getNode().getNetwork()).isNull();

        assertThat(host2.getNode().getNetwork())
                .isNotSameAs(host3.getNode().getNetwork())
                .isNotSameAs(host4.getNode().getNetwork())
                .isNotSameAs(host5.getNode().getNetwork())
                .isNotSameAs(unrelatedHost.getNode().getNetwork());

        assertThat(NetworkUtil.getNetworkSplits(host2.getNode().getNetwork())).isEmpty();
        assertThat(NetworkUtil.getAddedHosts(host2.getNode().getNetwork())).containsExactlyInAnyOrder(host2);
        assertThat(NetworkUtil.getRemovedHosts(host2.getNode().getNetwork())).isEmpty();
        assertThat(NetworkUtil.getNetworkRemovedCount(host2.getNode().getNetwork())).isZero();

        assertThat(host3.getNode().getNetwork())
                .isNotSameAs(host2.getNode().getNetwork())
                .isNotSameAs(host4.getNode().getNetwork())
                .isNotSameAs(host5.getNode().getNetwork())
                .isNotSameAs(unrelatedHost.getNode().getNetwork());

        List<Set<Network>> splits = NetworkUtil.getNetworkSplits(host3.getNode().getNetwork());
        assertThat(splits).hasSize(1);
        assertThat(splits.get(0)).containsExactlyInAnyOrder(host2.getNode().getNetwork(), host4.getNode().getNetwork());

        assertThat(NetworkUtil.getAddedHosts(host3.getNode().getNetwork())).containsExactlyInAnyOrder(host1, host2, host3, host4, host5);
        assertThat(NetworkUtil.getRemovedHosts(host3.getNode().getNetwork())).containsExactlyInAnyOrder(host1, host2, host4, host5);
        assertThat(NetworkUtil.getNetworkRemovedCount(host3.getNode().getNetwork())).isZero();

        assertThat(host4.getNode().getNetwork())
                .isNotSameAs(host2.getNode().getNetwork())
                .isNotSameAs(host3.getNode().getNetwork())
                .isSameAs(host5.getNode().getNetwork())
                .isNotSameAs(unrelatedHost.getNode().getNetwork());

        assertThat(NetworkUtil.getNetworkSplits(host4.getNode().getNetwork())).isEmpty();
        assertThat(NetworkUtil.getAddedHosts(host4.getNode().getNetwork())).containsExactlyInAnyOrder(host4, host5);
        assertThat(NetworkUtil.getRemovedHosts(host4.getNode().getNetwork())).isEmpty();
        assertThat(NetworkUtil.getNetworkRemovedCount(host4.getNode().getNetwork())).isZero();

        assertThat(host2.getNode().getNetwork().getComponent(GraphNetworkComponent.class).getHosts()).containsExactlyInAnyOrder(
                NetworkNodeHostEntry.create(host2)
        );

        assertThat(host3.getNode().getNetwork().getComponent(GraphNetworkComponent.class).getHosts()).containsExactlyInAnyOrder(
                NetworkNodeHostEntry.create(host3)
        );

        assertThat(host4.getNode().getNetwork().getComponent(GraphNetworkComponent.class).getHosts()).containsExactlyInAnyOrder(
                NetworkNodeHostEntry.create(host4),
                NetworkNodeHostEntry.create(host5)
        );
    }

    @Test
    void Test_should_remove_network() {
        // Arrange
        FakeRs2World world = new FakeRs2World();

        NetworkNodeHost<?> host = NetworkUtil.createHostWithNetwork(world, Position.ORIGIN);
        NetworkNodeHost<?> unrelatedHost = NetworkUtil.createHostWithNetwork(world, Position.ORIGIN.south().south());

        FakeNetworkNodeHostRepository repository = FakeNetworkNodeHostRepository.of(host, unrelatedHost);

        Network network = host.getNode().getNetwork();

        // Act
        repository.removeHost(world, host.getPosition());
        host.remove(repository, NetworkUtil.NETWORK_COMPONENT_REGISTRY);

        // Assert
        assertThat(host.getNode().getNetwork()).isNull();

        assertThat(unrelatedHost.getNode().getNetwork().getComponent(GraphNetworkComponent.class).getHosts()).containsExactlyInAnyOrder(
                NetworkNodeHostEntry.create(unrelatedHost)
        );

        assertThat(NetworkUtil.getNetworkSplits(network)).isEmpty();
        assertThat(NetworkUtil.getAddedHosts(network)).containsExactly(host);
        assertThat(NetworkUtil.getRemovedHosts(network)).isEmpty();
        assertThat(NetworkUtil.getNetworkRemovedCount(network)).isEqualTo(1);
    }
}
