package com.zeebo.libe

import com.zeebo.beryllium.OperationalTransform
import com.zeebo.beryllium.OperationalTransformNetwork
import com.zeebo.beryllium.OperationalTransformSite
import com.zeebo.lithium.mesh.MeshNode
import com.zeebo.lithium.mesh.Message

/**
 * User: eric
 * Date: 11/28/14
 */
class LithiumOperationalTransformNetwork extends OperationalTransformNetwork {

	static int siteId

	MeshNode meshNode

	Map<String, Integer> networks = [:].withDefault { 0 }
	Map<String, OperationalTransformSite> localSite = [:]

	LithiumOperationalTransformNetwork(MeshNode networkNode) {
		meshNode = networkNode
		meshNode.addMessageHandler new OperationalTransformMessageHandler(network: this)
	}

	def joinMeshNetwork(String networkName) {

		OperationalTransformSite site = new OperationalTransformSite()
		site.network = this
		site.sv << 0

		networks[networkName]++
		localSite[networkName] = site

		Message msg = new Message()
		msg.messageType = OperationalTransformMessageHandler.TYPE_REQUEST_NEW_SITE_ID
		msg.data.networkName = networkName
		meshNode.sendAll(msg)

		return site
	}

	void broadcast(OperationalTransformSite sendingNode, OperationalTransform trans) {
		Message message = new Message()
		message.messageType = OperationalTransformMessageHandler.TYPE_BERYLLIUM_OPERATIONAL_TRANSFORM
		message.data.networkName = localSite.find { k, v -> v == sendingNode }.key
		message.data.sendingNodeId = sendingNode.id
		message.data.trans = trans

		meshNode.sendAll(message)
	}

	public static void main(String[] args) {
		MeshNode n1 = new MeshNode('node1', 40026)
		MeshNode n2 = new MeshNode('node2', 40027)
		LithiumOperationalTransformNetwork a = new LithiumOperationalTransformNetwork(n1)
		LithiumOperationalTransformNetwork b = new LithiumOperationalTransformNetwork(n2)

		n1.listen()
		n2.listen()

		n1.connect('localhost', 40027)

		sleep 1000

		a.joinMeshNetwork('test')
		b.joinMeshNetwork('test')

		sleep 1000

		println a.localSite.test.sv
		println b.localSite.test.sv

		a.broadcast(a.localSite.test, a.localSite.test.createOperationalTransform(OperationalTransform.TYPE_INSERT, 'a', 0))

		sleep 1000

		println a.localSite.test.sv
		println b.localSite.test.sv
	}
}
