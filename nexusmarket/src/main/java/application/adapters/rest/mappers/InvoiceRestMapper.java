package application.adapters.rest.mappers;

import application.adapters.rest.dtos.responses.InvoiceResponseDTO;
import application.domain.models.Invoice;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class InvoiceRestMapper {

    public InvoiceRestMapper() {
    }

    public static InvoiceResponseDTO toResponse(Invoice invoice) {
        if (invoice == null) {
            return null;
        }
        return new InvoiceResponseDTO(
                invoice.getInvoiceId(),
                invoice.getOrder().getOrderId(),
                invoice.getIssueDate(),
                invoice.getSubtotal().amount(),
                invoice.getTax().amount(),
                invoice.getTotal().amount(),
                invoice.getTotal().currency().getCurrencyCode()
        );
    }

    public static List<InvoiceResponseDTO> toResponseList(List<Invoice> invoices) {
        if (invoices == null) {
            return Collections.emptyList();
        }
        return invoices.stream().map(InvoiceRestMapper::toResponse).toList();
    }
}
