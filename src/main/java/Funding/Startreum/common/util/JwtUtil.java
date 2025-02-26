package funding.startreum.common.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {

    private static final String SECRET_KEY = "ThisIsASecretKeyForJwtTokenForTestingPurposeOnly"; // 환경 변수로 관리 권장
    private static final long ACCESS_TOKEN_EXPIRATION = 1000 * 60 * 30; // 30분
    private static final long REFRESH_TOKEN_EXPIRATION = 1000L * 60 * 60 * 24 * 7; // 7일

    private final Key key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));


    // ✅ Refresh Token 만료 시간 Getter 추가
    public long getRefreshTokenExpiration() {
        return REFRESH_TOKEN_EXPIRATION;
    }


    // ✅ Access Token 생성 (role 값을 "ROLE_" 접두어 유지)
    public String generateAccessToken(String name, String email, String role) {
        String formattedRole = role.startsWith("ROLE_") ? role : "ROLE_" + role; // ✅ ROLE_ 접두어 유지
        return Jwts.builder()
                .setSubject(name) // ✅ subject에 name(ID) 저장
                .claim("email", email) // ✅ email을 claim으로 추가
                .claim("role", formattedRole) // ✅ ROLE_ 접두어 유지
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRATION))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // ✅ Refresh Token 생성
    public String generateRefreshToken(String name) {
        return Jwts.builder()
                .setSubject(name) // ✅ subject에 name(ID) 저장
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + REFRESH_TOKEN_EXPIRATION))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // ✅ Access Token 검증 (UserDetails 포함)
    public boolean validateToken(String token, UserDetails userDetails) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            String tokenUsername = claims.getSubject();
            //System.out.println("📌 토큰에서 추출한 사용자명: " + tokenUsername);

            boolean isValidUser = tokenUsername.equals(userDetails.getUsername());

            if (!isValidUser) {
                // System.out.println("❌ 토큰의 사용자명과 DB의 사용자명이 일치하지 않음.");
                return false;
            }

            return true; // ✅ 모든 검증을 통과하면 true 반환
        } catch (ExpiredJwtException e) {
            System.out.println("❌ Token expired: " + e.getMessage());
        } catch (MalformedJwtException e) {
            System.out.println("❌ Invalid token format: " + e.getMessage());
        } catch (SignatureException e) {
            System.out.println("❌ Invalid token signature: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("❌ Unknown error during token validation: " + e.getMessage());
        }
        return false;
    }

    // ✅ Refresh Token 검증 (UserDetails 필요 없음)
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException e) {
            System.out.println("❌ Token expired: " + e.getMessage());
        } catch (MalformedJwtException e) {
            System.out.println("❌ Invalid token format: " + e.getMessage());
        } catch (SignatureException e) {
            System.out.println("❌ Invalid token signature: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("❌ Unknown error during token validation: " + e.getMessage());
        }
        return false;
    }

    // ✅ 토큰에서 사용자 이름(name) 추출
    public String getNameFromToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject() // ✅ subject에서 name(ID) 추출
                    .toLowerCase(); // ✅ 항상 소문자로 변환
        } catch (Exception e) {
            System.out.println("❌ Token parsing error: " + e.getMessage());
            return null;
        }
    }

    // ✅ 토큰에서 email 추출
    public String getEmailFromToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .get("email", String.class); // ✅ claim에서 email 추출
        } catch (Exception e) {
            System.out.println("❌ Token parsing error: " + e.getMessage());
            return null;
        }
    }

    // ✅ 토큰에서 role(권한) 추출 (ROLE_ 접두어 유지)
    public String getRoleFromToken(String token) {
        try {
            String role = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .get("role", String.class);

            System.out.println("📌 JWT에서 추출된 역할: " + role);

            // ✅ ROLE_ 접두어가 없으면 자동 추가
            return (role != null && role.startsWith("ROLE_")) ? role : "ROLE_" + role;
        } catch (Exception e) {
            System.out.println("❌ Token에서 역할 추출 중 오류 발생: " + e.getMessage());
            return null;
        }
    }
}