package application.adapters.rest.mappers;

import application.adapters.rest.dtos.requests.AddressDTO;
import application.adapters.rest.dtos.responses.BuyerResponseDTO;
import application.adapters.rest.dtos.responses.SellerResponseDTO;
import application.adapters.rest.dtos.responses.UserResponseDTO;
import application.domain.models.Buyer;
import application.domain.models.Seller;
import application.domain.models.User;
import application.domain.valueobjects.Address;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class UserRestMapper {

    public UserRestMapper() {
    }

    public static UserResponseDTO toUserResponse(User user) {
        if (user == null) {
            return null;
        }
        return new UserResponseDTO(
                user.getUserId(),
                user.getFullName(),
                user.getEmail().value(),
                user.getIdentityDocument().type(),
                user.getIdentityDocument().number(),
                user.getRole(),
                user.getStatus()
        );
    }

    public static List<UserResponseDTO> toUserResponseList(List<User> users) {
        if (users == null) {
            return Collections.emptyList();
        }
        return users.stream().map(UserRestMapper::toUserResponse).toList();
    }

    public static BuyerResponseDTO toBuyerResponse(Buyer buyer) {
        if (buyer == null) {
            return null;
        }
        AddressDTO primaryAddressDto = OrderRestMapper.toAddressDto(buyer.getPrimaryAddress());
        List<AddressDTO> additionalAddressDtos = buyer.getAdditionalAddresses().stream()
                .map(OrderRestMapper::toAddressDto)
                .toList();

        return new BuyerResponseDTO(
                buyer.getUserId(),
                buyer.getFullName(),
                buyer.getEmail().value(),
                buyer.getIdentityDocument().type(),
                buyer.getIdentityDocument().number(),
                buyer.getRole(),
                buyer.getStatus(),
                primaryAddressDto,
                additionalAddressDtos,
                buyer.getCommercialStatus()
        );
    }

    public static SellerResponseDTO toSellerResponse(Seller seller) {
        if (seller == null) {
            return null;
        }
        return new SellerResponseDTO(
                seller.getUserId(),
                seller.getFullName(),
                seller.getEmail().value(),
                seller.getIdentityDocument().type(),
                seller.getIdentityDocument().number(),
                seller.getRole(),
                seller.getStatus(),
                seller.getOnboardingDate()
        );
    }

    public Address toAddress(AddressDTO dto) {
        return OrderRestMapper.toAddress(dto);
    }
}
