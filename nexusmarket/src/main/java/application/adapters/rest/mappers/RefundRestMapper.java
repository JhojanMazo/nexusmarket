package application.adapters.rest.mappers;

import application.adapters.rest.dtos.responses.RefundResponseDTO;
import application.domain.models.Refund;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class RefundRestMapper {

    public RefundResponseDTO toResponse(Refund refund) {
        if (refund == null) return null;
        return new RefundResponseDTO(
                refund.getRefundId(),
                refund.getReturnRequest() != null ? refund.getReturnRequest().getRequestId() : null,
                refund.getAmount().amount(),
                refund.getAmount().currency().getCurrencyCode(),
                refund.getStatus()
        );
    }

    public List<RefundResponseDTO> toResponseList(List<Refund> refunds) {
        if (refunds == null) return Collections.emptyList();
        return refunds.stream().map(this::toResponse).toList();
    }
}
