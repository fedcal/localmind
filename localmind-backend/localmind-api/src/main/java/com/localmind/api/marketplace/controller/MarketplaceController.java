package com.localmind.api.marketplace.controller;

import com.localmind.api.marketplace.dto.AddReviewRequest;
import com.localmind.api.marketplace.dto.MarketplaceAgentDto;
import com.localmind.api.marketplace.dto.MarketplaceReviewDto;
import com.localmind.api.marketplace.dto.PublishAgentRequest;
import com.localmind.domain.marketplace.model.AgentCategory;
import com.localmind.domain.marketplace.model.MarketplaceAgent;
import com.localmind.domain.marketplace.model.MarketplaceReview;
import com.localmind.domain.marketplace.port.in.MarketplaceUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/marketplace")
@Tag(name = "Marketplace", description = "Agent marketplace management")
public class MarketplaceController {

    private final MarketplaceUseCase marketplaceUseCase;

    public MarketplaceController(MarketplaceUseCase marketplaceUseCase) {
        this.marketplaceUseCase = marketplaceUseCase;
    }

    @GetMapping("/agents")
    @Operation(summary = "Lista agenti marketplace / List marketplace agents")
    @ApiResponse(responseCode = "200", description = "Lista agenti / Agent list")
    public ResponseEntity<List<MarketplaceAgentDto>> listAgents(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String search) {
        AgentCategory cat = category != null && !category.isBlank() ? AgentCategory.valueOf(category) : null;
        List<MarketplaceAgentDto> agents = marketplaceUseCase.listAgents(cat, search).stream()
                .map(this::toAgentDto)
                .toList();
        return ResponseEntity.ok(agents);
    }

    @GetMapping("/agents/{id}")
    @Operation(summary = "Dettaglio agente marketplace / Get marketplace agent detail")
    @ApiResponse(responseCode = "200", description = "Agente trovato / Agent found")
    @ApiResponse(responseCode = "404", description = "Non trovato / Not found")
    public ResponseEntity<MarketplaceAgentDto> getAgent(@PathVariable String id) {
        return marketplaceUseCase.getAgent(id)
                .map(this::toAgentDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/agents")
    @Operation(summary = "Pubblica agente / Publish agent")
    @ApiResponse(responseCode = "200", description = "Agente pubblicato / Agent published")
    @ApiResponse(responseCode = "400", description = "Richiesta non valida / Bad request")
    public ResponseEntity<MarketplaceAgentDto> publishAgent(@RequestBody PublishAgentRequest request) {
        MarketplaceAgent agent = MarketplaceAgent.builder()
                .name(request.name())
                .description(request.description())
                .author(request.author())
                .version(request.version())
                .category(AgentCategory.valueOf(request.category()))
                .downloadUrl(request.downloadUrl())
                .build();
        MarketplaceAgent saved = marketplaceUseCase.publishAgent(agent);
        return ResponseEntity.ok(toAgentDto(saved));
    }

    @PostMapping("/agents/{id}/install")
    @Operation(summary = "Installa agente / Install agent")
    @ApiResponse(responseCode = "200", description = "Agente installato / Agent installed")
    @ApiResponse(responseCode = "404", description = "Non trovato / Not found")
    public ResponseEntity<Void> installAgent(@PathVariable String id) {
        marketplaceUseCase.installAgent(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/agents/{id}/reviews")
    @Operation(summary = "Aggiungi recensione / Add review")
    @ApiResponse(responseCode = "200", description = "Recensione aggiunta / Review added")
    @ApiResponse(responseCode = "400", description = "Richiesta non valida / Bad request")
    @ApiResponse(responseCode = "404", description = "Non trovato / Not found")
    public ResponseEntity<MarketplaceReviewDto> addReview(
            @PathVariable String id,
            @RequestBody AddReviewRequest request) {
        MarketplaceReview review = marketplaceUseCase.addReview(id, request.rating(), request.comment());
        return ResponseEntity.ok(toReviewDto(review));
    }

    @GetMapping("/agents/{id}/reviews")
    @Operation(summary = "Lista recensioni agente / List agent reviews")
    @ApiResponse(responseCode = "200", description = "Lista recensioni / Review list")
    @ApiResponse(responseCode = "404", description = "Non trovato / Not found")
    public ResponseEntity<List<MarketplaceReviewDto>> getReviews(@PathVariable String id) {
        List<MarketplaceReviewDto> reviews = marketplaceUseCase.getReviews(id).stream()
                .map(this::toReviewDto)
                .toList();
        return ResponseEntity.ok(reviews);
    }

    @DeleteMapping("/agents/{id}")
    @Operation(summary = "Elimina agente / Delete agent")
    @ApiResponse(responseCode = "204", description = "Agente eliminato / Agent deleted")
    @ApiResponse(responseCode = "404", description = "Non trovato / Not found")
    public ResponseEntity<Void> deleteAgent(@PathVariable String id) {
        marketplaceUseCase.deleteAgent(id);
        return ResponseEntity.noContent().build();
    }

    private MarketplaceAgentDto toAgentDto(MarketplaceAgent agent) {
        return new MarketplaceAgentDto(
                agent.getId(),
                agent.getName(),
                agent.getDescription(),
                agent.getAuthor(),
                agent.getVersion(),
                agent.getCategory().name(),
                agent.getDownloadUrl(),
                agent.getDownloadCount(),
                agent.getAvgRating(),
                agent.getPublishedAt(),
                agent.getCreatedAt()
        );
    }

    private MarketplaceReviewDto toReviewDto(MarketplaceReview review) {
        return new MarketplaceReviewDto(
                review.getId(),
                review.getAgentId(),
                review.getRating(),
                review.getComment(),
                review.getCreatedAt()
        );
    }
}
