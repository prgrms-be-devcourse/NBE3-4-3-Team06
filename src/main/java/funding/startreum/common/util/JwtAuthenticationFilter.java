package funding.startreum.common.util;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtUtil jwtUtil, UserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");

        //System.out.println("🔍 요청 URL: " + request.getRequestURI());
       // System.out.println("🔍 Authorization 헤더: " + header);

        if (header == null || !header.startsWith("Bearer ")) {
          //  System.out.println("❌ Authorization 헤더가 없거나 잘못된 형식임.");
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.replace("Bearer ", "");
        String username = null;

        try {
            username = jwtUtil.getNameFromToken(token).trim();
          //  System.out.println("✅ JWT에서 추출된 사용자명: " + username);
        } catch (Exception e) {
           // System.out.println("❌ JWT에서 사용자명 추출 실패: " + e.getMessage());
            filterChain.doFilter(request, response);
            return;
        }

        // ✅ URL에서 사용자 이름 추출
        String requestURI = request.getRequestURI();
        if (requestURI.startsWith("/api/users/profile/")) {
            String requestedName = requestURI.replace("/api/users/profile/", "");
           // System.out.println("📌 요청된 프로필 사용자명: " + requestedName);

            // ✅ 요청한 사용자명과 JWT 사용자명이 다르면 403 Forbidden
            if (!requestedName.equals(username)) {
             //   System.out.println("❌ 프로필 접근 권한 없음! (본인만 접근 가능)");
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "자신의 프로필만 볼 수 있습니다.");
                return;
            }
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails;
            try {
                userDetails = userDetailsService.loadUserByUsername(username);
            } catch (Exception e) {
                //System.out.println("❌ 사용자 정보 조회 실패 (DB에 존재하지 않음): " + username);
                filterChain.doFilter(request, response);
                return;
            }

            if (jwtUtil.validateToken(token, userDetails)) {
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);
               // System.out.println("✅ SecurityContextHolder에 사용자 설정 완료: " + username);
            }
        }

        filterChain.doFilter(request, response);
    }
}
