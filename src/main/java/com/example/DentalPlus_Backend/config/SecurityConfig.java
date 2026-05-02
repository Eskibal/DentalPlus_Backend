package com.example.DentalPlus_Backend.config;

import com.example.DentalPlus_Backend.dao.AdminDao;
import com.example.DentalPlus_Backend.dao.DentistDao;
import com.example.DentalPlus_Backend.dao.PatientDao;
import com.example.DentalPlus_Backend.dao.ReceptionistDao;
import com.example.DentalPlus_Backend.model.Admin;
import com.example.DentalPlus_Backend.model.Dentist;
import com.example.DentalPlus_Backend.model.Patient;
import com.example.DentalPlus_Backend.model.Receptionist;
import com.example.DentalPlus_Backend.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

	private final JwtService jwtService;
	private final AdminDao adminDao;
	private final DentistDao dentistDao;
	private final ReceptionistDao receptionistDao;
	private final PatientDao patientDao;

	public SecurityConfig(JwtService jwtService, AdminDao adminDao, DentistDao dentistDao,
			ReceptionistDao receptionistDao, PatientDao patientDao) {
		this.jwtService = jwtService;
		this.adminDao = adminDao;
		this.dentistDao = dentistDao;
		this.receptionistDao = receptionistDao;
		this.patientDao = patientDao;
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

		OncePerRequestFilter jwtFilter = new OncePerRequestFilter() {
			@Override
			protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
					FilterChain filterChain) throws ServletException, IOException {

				String authorizationHeader = request.getHeader("Authorization");
				String token = jwtService.extractToken(authorizationHeader);

				System.out.println("[JWT] " + request.getMethod() + " " + request.getRequestURI());
				System.out.println("[JWT] Authorization header exists: " + (authorizationHeader != null));
				System.out.println("[JWT] Token exists: " + (token != null));

				if (token != null && jwtService.validateToken(token)) {
					Long userId = jwtService.extractUserId(token);

					List<SimpleGrantedAuthority> authorities = buildAuthorities(userId);

					UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userId,
							null, authorities);

					SecurityContextHolder.getContext().setAuthentication(authentication);

					System.out.println("[JWT] Valid token for userId: " + userId);
					System.out.println("[JWT] Authorities: " + authorities);
				} else {
					SecurityContextHolder.clearContext();
					System.out.println("[JWT] No valid token found");
				}

				filterChain.doFilter(request, response);
			}
		};

		http.csrf(csrf -> csrf.disable()).cors(cors -> cors.disable())
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authorizeHttpRequests(auth -> auth.requestMatchers(HttpMethod.POST, "/user/login").permitAll()
						.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll().requestMatchers("/error").permitAll()
						.anyRequest().authenticated())
				.exceptionHandling(ex -> ex.authenticationEntryPoint((request, response, authException) -> {
					response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
					response.setContentType("text/plain;charset=UTF-8");
					response.getWriter().write("Unauthorized: missing or invalid token");
				}).accessDeniedHandler((request, response, accessDeniedException) -> {
					response.setStatus(HttpServletResponse.SC_FORBIDDEN);
					response.setContentType("text/plain;charset=UTF-8");
					response.getWriter().write("Forbidden: authenticated but not allowed");
				})).addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
				.httpBasic(httpBasic -> httpBasic.disable()).formLogin(form -> form.disable())
				.logout(logout -> logout.disable());

		return http.build();
	}

	private List<SimpleGrantedAuthority> buildAuthorities(Long userId) {
		List<SimpleGrantedAuthority> authorities = new ArrayList<>();

		Admin admin = adminDao.findByUserId(userId);
		if (admin != null && Boolean.TRUE.equals(admin.getActive())) {
			authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
		}

		Dentist dentist = dentistDao.findByUserId(userId);
		if (dentist != null && Boolean.TRUE.equals(dentist.getActive())) {
			authorities.add(new SimpleGrantedAuthority("ROLE_DENTIST"));
		}

		Receptionist receptionist = receptionistDao.findByUserId(userId);
		if (receptionist != null && Boolean.TRUE.equals(receptionist.getActive())) {
			authorities.add(new SimpleGrantedAuthority("ROLE_RECEPTIONIST"));
		}

		Patient patient = patientDao.findByUserId(userId);
		if (patient != null && Boolean.TRUE.equals(patient.getActive())) {
			authorities.add(new SimpleGrantedAuthority("ROLE_PATIENT"));
		}

		return authorities;
	}

	@Bean
	public BCryptPasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
}