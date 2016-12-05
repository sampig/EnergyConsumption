# Copyright (C) 2016 Chenfeng Zhu
#
# This file is part of the "Energy Consumption" project.
#
# This file is used to generate data in a certain rate.
#

# system
import binascii
import signal
import sys
import threading
import time

# third-party
from ostinato.core import ost_pb, DroneProxy
from ostinato.protocols.mac_pb2 import mac
from ostinato.protocols.ip4_pb2 import ip4  # , Ip4
from ostinato.protocols.textproto_pb2 import textProtocol
from ostinato.protocols.udp_pb2 import udp
# from ostinato.protocols.payload_pb2 import payload, Payload
from ostinato.protocols.hexdump_pb2 import hexDump

# user-defined
from ecclient.conf import properties_reader
from ecclient.utils import devices_reader

def logStr():
    return "Data Generation [" + time.strftime("%Y-%m-%d %H:%M:%S") + "]: "

''' start data generation '''
def startDataGeneration(num=1, freq=44000):
    global dataGen, mac_addresses
    mac_addresses = devices_reader.getDevices()[0]
    num_devices = len(mac_addresses)
    if (num < 1):
        num = num_devices
    
    dataGen = DataGeneratorOstinato()
    dataGen.create_stream(num)

    i = 0
    num_packets = 1  # 000
    packets_per_sec = num * freq  # 000
    #src_mac = int(mac_addresses[i], 16) #0x00aabbccddee 733295205870
    dst_mac = 0x000000000000
    src_ip = 0x01020304
    dst_ip = 0x7f000001
    src_port = 80
    dst_port = 17878
    print logStr(), "Starting data generation by Ostinato..."
    print "\tnumber of devices: " + str(num)
    print "\tfrequency of data generation: " + str(freq) + "\n"
    while i < num:
        try:
            if i == num - 1:
                dataGen.setStreamcontrolParameters(num_packets, packets_per_sec, False)
            else:
                dataGen.setStreamcontrolParameters(num_packets, packets_per_sec, True)
            src_mac = int(mac_addresses[i], 16)
            dataGen.setProtocolSourceParameters(src_mac, src_ip, src_port)
            dataGen.setProtocolDestinationParameters(dst_mac, dst_ip, dst_port)
            dataGen.add_stream(i)
            #dst_ports.append(dst_port)
            i += 1
            #src_mac += 1#src_mac = int(mac_addresses[i], 16) #
            #dst_port += 1
        except:
            print logStr(), "Error"
            # dataGen.stream_stop()
            exit
    dataGen.start_stream()

''' stop data generation '''
def stopDataGeneration():
    time.sleep(10)
    print logStr(), "Stopping data generation..."
    try:
        dataGen.stop_stream()
        dataGen.disconnect()
        sys.exit(0)
    except:
        pass
    finally:
        sys.exit(0)

''' handle with CTRL+C '''
def sigint_handler(signum, frame):
    stopDataGeneration()
    print logStr(), "Stop pressing the CTRL+C!"


'''
This class use Ostinato to create packets
'''
class DataGeneratorOstinato(object):

    HOST_NAME = '127.0.0.1'  # host ip for the drone
    HOST_PORT_NUMBER = 7878  # host port for the drone
    # get the config from property file
    HOST_NAME, HOST_PORT_NUMBER = properties_reader.getOstinatoConfigHost()

    TX_PORT_NUMBER = 2  # ports for different devices
    RX_PORT_NUMBER = 2  # ports for different devices
    '''
    # list all ports for devices
    port_id_list = drone.getPortIdList()
    port_config_list = drone.getPortConfig(port_id_list)
    
    print('Port List')
    print('---------')
    for port in port_config_list.port:
        print('%d.%s (%s)' % (port.port_id.id, port.name, port.description))
    
    # 0.wlp2s0 ()
    # 1.any (Pseudo-device that captures on all interfaces)
    # 2.lo ()
    # 3.docker0 ()
    # 4.enp0s25 ()
    # 5.br-2736930d2396 ()
    # 6.bluetooth0 (Bluetooth adapter number 0)
    '''

    # configuration of stream control
    num_packets = 100  # 000
    packets_per_sec = 44  # 000
    next_stream = ost_pb.StreamControl.e_nw_goto_id
    # configuration of protocol
    src_mac = 0x00aabbccddee
    dst_mac = 0x000000000000
    src_ip = 0x01020304
    dst_ip = 0x7f000001  # binascii.hexlify(socket.inet_aton('127.0.0.1')) #
    src_port = 80
    dst_port = 17878
    # content
    text = ''
    payload_content = binascii.unhexlify('0001591e14')  # 0xfedcba98 #

    def __init__(self, tx_port=2, rx_port=2):
        self.TX_PORT_NUMBER = tx_port
        self.RX_PORT_NUMBER = rx_port

        self.drone = DroneProxy(self.HOST_NAME, self.HOST_PORT_NUMBER)
        self.drone.connect()

        # transmit port
        self.tx_port = ost_pb.PortIdList()
        self.tx_port.port_id.add().id = self.TX_PORT_NUMBER

        # receive port
        self.rx_port = ost_pb.PortIdList()
        self.rx_port.port_id.add().id = self.RX_PORT_NUMBER

    ''' set parameters of configuration for stream control '''
    def setStreamcontrolParameters(self, num, packets, isnext=True):
        self.num_packets = num
        self.packets_per_sec = packets
        if isnext:  # go to next stream
            self.next_stream = ost_pb.StreamControl.e_nw_goto_next
        else:  # restart from the first stream
            self.next_stream = ost_pb.StreamControl.e_nw_goto_id

    ''' set parameters of configuration for protocol '''
    def setProtocolParameters(self, smac, dmac, sip, dip, sport, dport):
        self.src_ip = sip
        self.dst_ip = dip
        self.src_mac = smac
        self.dst_mac = dmac
        self.src_port = sport
        self.dst_port = dport
    def setProtocolSourceParameters(self, smac, sip, sport):
        self.src_ip = sip
        self.src_mac = smac
        self.src_port = sport
    def setProtocolDestinationParameters(self, dmac, dip, dport):
        self.dst_ip = dip
        self.dst_mac = dmac
        self.dst_port = dport

    ''' set payload data in the packet '''
    def setPayload(self, t, d):
        self.text = t
        self.payload_content = d

    ''' create a new stream '''
    def create_stream(self, num_streams):
        self.stream_id = ost_pb.StreamIdList()
        self.stream_id.port_id.CopyFrom(self.tx_port.port_id[0])
        i = 1
        while i <= num_streams:
            self.stream_id.stream_id.add().id = i
            i += 1
        self.drone.addStream(self.stream_id)

        self.stream_cfg = ost_pb.StreamConfigList()
        self.stream_cfg.port_id.CopyFrom(self.tx_port.port_id[0])

        # print "***stream_id: ", self.stream_id, "\n***\n\n"

    ''' config the stream '''
    def add_stream(self, sid=0):
        # config the stream
        s = self.stream_cfg.stream.add()
        s.stream_id.id = self.stream_id.stream_id[sid].id
        s.core.is_enabled = True
        s.control.num_packets = self.num_packets  # number of packets
        s.control.packets_per_sec = self.packets_per_sec  # rate: packets/s
        s.control.next = self.next_stream
        # print "stream_cfg: ", stream_cfg

        # setup stream protocols as mac:eth2:ip4:udp:payload
        # mac
        p = s.protocol.add()
        p.protocol_id.id = ost_pb.Protocol.kMacFieldNumber
        p.Extensions[mac].dst_mac = self.dst_mac
        p.Extensions[mac].src_mac = self.src_mac

        # Eth II
        p = s.protocol.add()
        p.protocol_id.id = ost_pb.Protocol.kEth2FieldNumber

        # IPv4
        p = s.protocol.add()
        p.protocol_id.id = ost_pb.Protocol.kIp4FieldNumber
        ip = p.Extensions[ip4]
        ip.src_ip = self.src_ip
        ip.dst_ip = self.dst_ip
        # ip.dst_ip_mode = Ip4.e_im_inc_host

        # UDP
        p = s.protocol.add()
        p.protocol_id.id = ost_pb.Protocol.kUdpFieldNumber
        p.Extensions[udp].is_override_dst_port = True
        p.Extensions[udp].dst_port = self.dst_port

        # Text Protocol
        p = s.protocol.add()
        p.protocol_id.id = ost_pb.Protocol.kTextProtocolFieldNumber
        p.Extensions[textProtocol].text = self.text

        # Payload (Payload Data)
        # p.protocol_id.id = ost_pb.Protocol.kPayloadFieldNumber
        # p.Extensions[payload].pattern_mode = Payload.e_dp_fixed_word #e_dp_random
        # p.Extensions[payload].pattern = self.payload_content
        # Payload (Hex Dump)
        p = s.protocol.add()
        p.protocol_id.id = ost_pb.Protocol.kHexDumpFieldNumber
        p.Extensions[hexDump].content = self.payload_content

        self.drone.modifyStream(self.stream_cfg)

        # print "***stream_cfg: ", stream_cfg, "\n\n\n"
        # print "###\nstream: \n", s, "\n###\n"

        return self.stream_id

    # time.sleep(10)

    ''' start the stream '''
    def start_stream(self):
        # print "***stream_cfg: ", self.stream_cfg, "\n***\n\n"

        self.drone.clearStats(self.tx_port)
        self.drone.clearStats(self.rx_port)

        self.drone.startCapture(self.rx_port)
        self.drone.startTransmit(self.tx_port)

    ''' stop the stream '''
    def stop_stream(self):
        self.drone.stopTransmit(self.tx_port)
        self.drone.stopCapture(self.rx_port)

        # tx_stats = drone.getStats(tx_port)
        # rx_stats = drone.getStats(rx_port)

    ''' disconnect drone '''
    def disconnect(self):
        self.drone.deleteStream(self.stream_id)

        self.drone.disconnect()

if __name__ == "__main__":
    startDataGeneration(0)
    signal.signal(signal.SIGINT, sigint_handler)
    threading.Thread(target=stopDataGeneration, args=[]).start()

