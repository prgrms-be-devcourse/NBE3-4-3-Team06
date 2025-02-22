package Funding.Startreum.util;

import Funding.Startreum.common.util.JwtUtil;

public class TokenUtil {
    
    public static String createUserToken(JwtUtil jwtUtil, String username, String email, String role) {
        return jwtUtil.generateAccessToken(username, email, role);
    }
}
