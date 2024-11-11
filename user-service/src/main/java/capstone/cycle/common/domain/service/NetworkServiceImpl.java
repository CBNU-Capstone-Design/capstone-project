package capstone.cycle.common.domain.service;

import org.springframework.stereotype.Service;

import java.net.InetAddress;

@Service
public class NetworkServiceImpl implements NetworkService{
    @Override
    public boolean isLocal() {
        InetAddress localhost;
//		try {
//			localhost = InetAddress.getLocalHost();
//			String localhostIP = localhost.getHostAddress();
//
//			if("127.0.0.1".equals(localhostIP) || "localhost".equals(localhostIP) || "172.19.112.1".equals(localhostIP)) {
//				return true;
//			} else {
//				return false;
//			}
//		} catch (UnknownHostException e) {
//			e.printStackTrace();
//			new UnknownHostException("unknown host exception occured");
//		}
        return true;
    }
}
