package com.example.demo.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.util.Arrays;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
	
/*
    @Autowired
    JwtService jwtService;
    @Autowired
    UserDetailsService userDetailsService;


    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
    	
    	// Récupère l'en-tete d'authorization de la requete
        final String authHeader = request.getHeader("Authorization");
        // Vérifie que l'en-tete est bien présente et contient "Bearer"
        if (authHeader == null || !authHeader.startsWith("Bearer ")){
            filterChain.doFilter(request, response);
            return;
        }
        
        // Extrait le jwt et le nom d'utilisateur
        final String jwt = authHeader.substring("Bearer ".length());
        final String email = jwtService.extractUsername(jwt);
        // Si l'username est null ou que l'user est dej authentifié la requete s'arrete
        if (email == null || SecurityContextHolder.getContext().getAuthentication() !=null){
            filterChain.doFilter(request, response);
            return;
        }
        
        // Si le token est pas valide par ce que l'username ne correspond pas ou qu'il est expiré, la requete s'arrete
        UserDetails userDetails = this.userDetailsService.loadUserByUsername(email);
        if (!jwtService.isTokenValid(jwt, userDetails)){
        	System.out.println(jwt);
            filterChain.doFilter(request, response);
            return;
        }
        
        
        // Sinon l'authentification est générée
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities()
        );
        // L'identité de l'user et ses roles sont récupéré
        authToken.setDetails(
                new WebAuthenticationDetailsSource().buildDetails(request)
        );
        SecurityContextHolder.getContext().setAuthentication(authToken);
        // Transmission du authToken dans l'authentification
        filterChain.doFilter(request, response);
    }
  */
	
	
	@Autowired
    JwtService jwtService;

    @Autowired
    UserDetailsService userDetailsService;
   
    
    
    private void clearJwtCookie(HttpServletResponse response) {
        ResponseCookie clearCookie = ResponseCookie.from("jwt", "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0)
                .sameSite("Lax")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, clearCookie.toString());
    }


    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String path = request.getRequestURI();

        // 🚫 Ne pas appliquer le filtre sur la route de login
        if (path.startsWith("/f_all")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 🔍 Lecture du cookie "jwt"
        String jwt = null;
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            jwt = Arrays.stream(cookies)
                    .filter(c -> "jwt".equals(c.getName()))
                    .findFirst()
                    .map(Cookie::getValue)
                    .orElse(null);
        }

        // ⛔ Si aucun token trouvé, continuer sans authentification
        if (jwt == null) {
            filterChain.doFilter(request, response);
            return;
        }

        // ✅ Extraction de l'email
        String email = null;
        try {
            email = jwtService.extractUsername(jwt);
        } catch (Exception e) {
            // ❌ Le token est peut-être expiré ou invalide
            filterChain.doFilter(request, response);
            return;
        }
        
        // 🔥 Si le token est expiré → supprimer le cookie
        if (jwtService.isTokenExpired(jwt)) {
            clearJwtCookie(response);
            filterChain.doFilter(request, response);
            return;
        }

        // ✅ Authentification si nécessaire
        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(email);

            if (jwtService.isTokenValid(jwt, userDetails)) {
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        filterChain.doFilter(request, response);
    }

}