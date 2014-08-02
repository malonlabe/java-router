/*
 * Created on Aug 22, 2008
 */
package code.messy.net.ip.route;

import java.io.IOException;
import java.net.InetAddress;

import code.messy.Handler;
import code.messy.net.ip.IpPacket;

public class RouteHandler implements Handler<IpPacket> {
    @Override
    public void handle(IpPacket ip) {
        InetAddress dst = ip.getDestinationAddress();
        Subnet subnet = RoutingTable.getInstance().getSubnetByMasking(dst);
        if (subnet != null) {
            try {
                subnet.forward(ip);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}