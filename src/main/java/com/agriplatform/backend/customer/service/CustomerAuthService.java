package com.agriplatform.backend.customer.service;

import com.agriplatform.backend.auth.service.JwtService;
import com.agriplatform.backend.customer.dto.CustomerAuthResponse;
import com.agriplatform.backend.customer.dto.CustomerGoogleAuthRequest;
import com.agriplatform.backend.customer.dto.CustomerLoginRequest;
import com.agriplatform.backend.customer.dto.CustomerProfileResponse;
import com.agriplatform.backend.customer.dto.CustomerSignupRequest;
import com.agriplatform.backend.customer.model.Customer;
import com.agriplatform.backend.customer.repository.CustomerRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomerAuthService {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CustomerAuthService.class);

    private static final String CUSTOMER_SUBJECT_PREFIX = "CUSTOMER:";

    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;
    private final String googleClientId;

    public CustomerAuthService(
            CustomerRepository customerRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            ObjectMapper objectMapper,
            @Value("${app.customer.google.client-id:}") String googleClientId
    ) {
        this.customerRepository = customerRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newHttpClient();
        this.googleClientId = googleClientId == null ? "" : googleClientId.trim();
    }

    @Transactional
    public CustomerAuthResponse signup(CustomerSignupRequest request) {
        String email = normalizeEmail(request.email());
        if (customerRepository.findByEmailIgnoreCase(email).isPresent()) {
            throw new IllegalArgumentException("Email is already registered");
        }

        Customer customer = new Customer(
                request.fullName().trim(),
                normalizeCompanyName(request.companyName(), request.fullName()),
                email,
                request.phone().trim(),
                "",
                "",
                "",
                ""
        );
        customer.updateProfile(
                request.fullName().trim(),
                normalizeCompanyName(request.companyName(), request.fullName()),
                email,
                request.phone().trim(),
                "",
                "",
                "",
                ""
        );
        customer.setLocalAuth(passwordEncoder.encode(request.password()));
        customer.setEmailVerified(true);
        Customer saved = customerRepository.save(customer);
        return buildAuthResponse(saved);
    }

    @Transactional(readOnly = true)
    public CustomerAuthResponse login(CustomerLoginRequest request) {
        String email = normalizeEmail(request.email());
        Customer customer = customerRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        if (!customer.isActive() || customer.getPasswordHash() == null
                || !passwordEncoder.matches(request.password(), customer.getPasswordHash())) {
            throw new BadCredentialsException("Invalid email or password");
        }
        return buildAuthResponse(customer);
    }

    @Transactional
    public CustomerAuthResponse googleAuth(CustomerGoogleAuthRequest request) {
        GoogleTokenProfile profile = verifyGoogleIdToken(request.idToken());

        if (!googleClientId.isBlank() && !googleClientId.equals(profile.aud())) {
            throw new IllegalArgumentException("Invalid Google client id");
        }

        String email = normalizeEmail(profile.email());
        Customer customer = customerRepository.findByGoogleSubject(profile.sub())
                .or(() -> customerRepository.findByEmailIgnoreCase(email))
                .orElseGet(() -> new Customer(
                        profile.name() == null || profile.name().isBlank() ? "Google User" : profile.name().trim(),
                        "Individual",
                        email,
                        "",
                        "",
                        "",
                        "",
                        ""
                ));

        customer.updateProfile(
                profile.name() == null || profile.name().isBlank() ? customer.getFullName() : profile.name().trim(),
                customer.getCompanyName() == null || customer.getCompanyName().isBlank() ? "Individual" : customer.getCompanyName(),
                email,
                customer.getPhone() == null ? "" : customer.getPhone(),
                customer.getDeliveryAddress() == null ? "" : customer.getDeliveryAddress(),
                customer.getCity() == null ? "" : customer.getCity(),
                customer.getState() == null ? "" : customer.getState(),
                customer.getPostalCode() == null ? "" : customer.getPostalCode()
        );
        customer.setGoogleAuth(profile.sub());
        customer.setActive(true);
        Customer saved = customerRepository.save(customer);
        return buildAuthResponse(saved);
    }

    public Long parseCustomerIdFromSubject(String subject) {
        if (subject == null || !subject.startsWith(CUSTOMER_SUBJECT_PREFIX)) {
            throw new IllegalArgumentException("Invalid customer token");
        }
        try {
            return Long.parseLong(subject.substring(CUSTOMER_SUBJECT_PREFIX.length()));
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Invalid customer token", ex);
        }
    }

    private CustomerAuthResponse buildAuthResponse(Customer customer) {
        String token = jwtService.generateToken(CUSTOMER_SUBJECT_PREFIX + customer.getId(), "CUSTOMER");
        return new CustomerAuthResponse(
                token,
                "Bearer",
                jwtService.getExpirationSeconds(),
                "CUSTOMER",
                new CustomerProfileResponse(
                        customer.getId(),
                        customer.getFullName(),
                        customer.getCompanyName(),
                        customer.getEmail(),
                        customer.getPhone(),
                        customer.getDeliveryAddress(),
                        customer.getCity(),
                        customer.getState(),
                        customer.getPostalCode(),
                        customer.getPreferredPaymentMethod(),
                        customer.getPreferredPaymentHandle(),
                        customer.isDeferredPaymentEligible()
                )
        );
    }

    private GoogleTokenProfile verifyGoogleIdToken(String idToken) {
        if (idToken == null || idToken.isBlank()) {
            throw new IllegalArgumentException("Google token is required");
        }
        String encoded = URLEncoder.encode(idToken.trim(), StandardCharsets.UTF_8);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://oauth2.googleapis.com/tokeninfo?id_token=" + encoded))
                .GET()
                .build();

        HttpResponse<String> response;
        try {
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalArgumentException("Unable to verify Google login", ex);
        }

        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IllegalArgumentException("Invalid Google token");
        }

        try {
            JsonNode root = objectMapper.readTree(response.body());
            String sub = textValue(root, "sub");
            String email = textValue(root, "email");
            String aud = textValue(root, "aud");
            String name = root.path("name").asText("");
            if (sub.isBlank() || email.isBlank()) {
                throw new IllegalArgumentException("Invalid Google token payload");
            }
            return new GoogleTokenProfile(sub, email, aud, name);
        } catch (IOException ex) {
            throw new IllegalArgumentException("Invalid Google token payload", ex);
        }
    }

    private String textValue(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return value == null ? "" : value.asText("").trim();
    }

    private String normalizeEmail(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeCompanyName(String companyName, String fullName) {
        if (companyName == null || companyName.trim().isBlank()) {
            return "Individual";
        }
        return companyName.trim();
    }

    private record GoogleTokenProfile(String sub, String email, String aud, String name) {
    }
}
