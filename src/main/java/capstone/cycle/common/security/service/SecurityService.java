package capstone.cycle.common.security.service;

import capstone.cycle.user.dto.UserDTO;

public interface SecurityService {

    public void saveUserInSecurityContext(UserDTO userDTO);

    public void saveUserInSecurityContext(String accessToken);

    public UserDTO getUserInfoSecurityContext();
}
