package application.adapters.rest;

import application.adapters.rest.controllers.AuthRestController;
import application.adapters.rest.exception.GlobalExceptionHandler;
import application.adapters.rest.mappers.UserRestMapper;
import application.domain.enums.UserRole;
import application.domain.models.AuthenticationResult;
import application.domain.models.Buyer;
import application.domain.ports.in.PublicAccessPort;
import application.domain.valueobjects.Address;
import application.domain.valueobjects.Email;
import application.domain.valueobjects.IdentityDocument;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AuthRestControllerTest {

    private MockMvc mockMvc;

    @Mock
    private PublicAccessPort publicAccessPort;

    @BeforeEach
    void setUp() {
        AuthRestController controller = new AuthRestController(publicAccessPort, new UserRestMapper());
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void login_Success() throws Exception {
        UUID userId = UUID.randomUUID();
        when(publicAccessPort.authenticate("user@nexusmarket.com", "Password123!"))
                .thenReturn(new AuthenticationResult(userId, "John User", "user@nexusmarket.com", UserRole.BUYER, "sample.jwt.token"));

        String json = """
                {
                    "email": "user@nexusmarket.com",
                    "password": "Password123!"
                }
                """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("sample.jwt.token"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.email").value("user@nexusmarket.com"))
                .andExpect(jsonPath("$.role").value("BUYER"));
    }

    @Test
    void registerBuyer_Success() throws Exception {
        Address address = new Address("Cra 1", "Bogota", "DC", "Colombia", "1101", null);
        Buyer buyer = Buyer.create("Jane Doe", new Email("jane@nexus.com"), new IdentityDocument("CC", "12345"), address);

        when(publicAccessPort.registerBuyer(eq("Jane Doe"), eq("jane@nexus.com"), eq("CC"), eq("12345"), any(Address.class)))
                .thenReturn(buyer);

        String json = """
                {
                    "fullName": "Jane Doe",
                    "email": "jane@nexus.com",
                    "documentType": "CC",
                    "documentNumber": "12345",
                    "primaryAddress": {
                        "street": "Cra 1",
                        "city": "Bogota",
                        "stateOrProvince": "DC",
                        "country": "Colombia",
                        "postalCode": "1101"
                    }
                }
                """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.fullName").value("Jane Doe"))
                .andExpect(jsonPath("$.email").value("jane@nexus.com"));
    }
}
