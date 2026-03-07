package com.example.demo.security;

import com.example.demo.entity.Users;
import com.example.demo.repository.UsersRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UsersRepository usersRepository;

    public JwtAuthenticationFilter(JwtUtil jwtUtil,
                                   UsersRepository usersRepository) {
        this.jwtUtil = jwtUtil;
        this.usersRepository = usersRepository;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        // Bỏ qua cả OPTIONS request để tránh lỗi CORS
        return path.startsWith("/api/auth") || request.getMethod().equals("OPTIONS");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");
        System.out.println("JwtAuthenticationFilter: processing " + request.getMethod() + " " + request.getRequestURI());

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            System.out.println("JwtAuthenticationFilter: No Bearer token found, continuing filter chain");
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String token = authHeader.substring(7);
            System.out.println("JwtAuthenticationFilter: Token extracted: " + token.substring(0, Math.min(20, token.length())) + "...");

            if (!jwtUtil.validateToken(token)) {
                System.out.println("JwtAuthenticationFilter: Token validation failed");
                filterChain.doFilter(request, response);
                return;
            }
            System.out.println("JwtAuthenticationFilter: Token validated successfully");

            String username = jwtUtil.extractUsername(token);
            System.out.println("JwtAuthenticationFilter: Username from token: " + username);

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                Users user = usersRepository.findByUsername(username).orElse(null);
                if (user == null) {
                    System.out.println("JwtAuthenticationFilter: User not found in database: " + username);
                } else {
                    System.out.println("JwtAuthenticationFilter: User found: " + user.getUsername() + ", role: " + user.getRole());

                    String role = user.getRole();
                    if (!role.startsWith("ROLE_")) {
                        role = "ROLE_" + role;
                    }

                    SimpleGrantedAuthority authority = new SimpleGrantedAuthority(role);
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(username, null, Collections.singletonList(authority));
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    System.out.println("JwtAuthenticationFilter: Authentication set for user: " + username);
                }
            }

        } catch (Exception e) {
            System.out.println("JwtAuthenticationFilter: Exception during authentication: " + e.getMessage());
            e.printStackTrace();
        }

        filterChain.doFilter(request, response);
    }
}