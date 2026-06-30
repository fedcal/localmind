export interface SidebarDoc {
  slug: string;
  labels: Record<string, string>;
  path: string;
}

export interface SidebarCategory {
  id: string;
  labels: Record<string, string>;
  position: number;
  docs: SidebarDoc[];
}

export const SIDEBAR_DATA: SidebarCategory[] = [
  {
    "id": "01-project-overview",
    "labels": {
      "en": "Project Overview",
      "it": "Overview Progetto"
    },
    "position": 1,
    "docs": [
      {
        "slug": "vision-and-objectives",
        "labels": {
          "en": "Vision And Objectives",
          "it": "Visione E Obiettivi"
        },
        "path": "/docs/01-project-overview/vision-and-objectives"
      },
      {
        "slug": "target-users",
        "labels": {
          "en": "Target Users",
          "it": "Target Utenti"
        },
        "path": "/docs/01-project-overview/target-users"
      },
      {
        "slug": "value-proposition",
        "labels": {
          "en": "Value Proposition",
          "it": "Proposta Di Valore"
        },
        "path": "/docs/01-project-overview/value-proposition"
      }
    ]
  },
  {
    "id": "02-competitor-analysis",
    "labels": {
      "en": "Competitor Analysis",
      "it": "Analisi Competitor"
    },
    "position": 2,
    "docs": [
      {
        "slug": "market-overview",
        "labels": {
          "en": "Market Overview",
          "it": "Panoramica Mercato"
        },
        "path": "/docs/02-competitor-analysis/market-overview"
      },
      {
        "slug": "detailed-comparison",
        "labels": {
          "en": "Detailed Comparison",
          "it": "Confronto Dettagliato"
        },
        "path": "/docs/02-competitor-analysis/detailed-comparison"
      },
      {
        "slug": "localmind-differentiators",
        "labels": {
          "en": "Localmind Differentiators",
          "it": "Differenziatori Localmind"
        },
        "path": "/docs/02-competitor-analysis/localmind-differentiators"
      },
      {
        "slug": "openclaw-comparison",
        "labels": {
          "en": "Openclaw Comparison",
          "it": "Confronto Openclaw"
        },
        "path": "/docs/02-competitor-analysis/openclaw-comparison"
      }
    ]
  },
  {
    "id": "03-functional-analysis",
    "labels": {
      "en": "Functional Analysis",
      "it": "Analisi Funzionale"
    },
    "position": 3,
    "docs": [
      {
        "slug": "llm-gateway",
        "labels": {
          "en": "Llm Gateway",
          "it": "Llm Gateway"
        },
        "path": "/docs/03-functional-analysis/llm-gateway"
      },
      {
        "slug": "document-intelligence-rag",
        "labels": {
          "en": "Document Intelligence Rag",
          "it": "Document Intelligence Rag"
        },
        "path": "/docs/03-functional-analysis/document-intelligence-rag"
      },
      {
        "slug": "ai-agents",
        "labels": {
          "en": "Ai Agents",
          "it": "Ai Agents"
        },
        "path": "/docs/03-functional-analysis/ai-agents"
      },
      {
        "slug": "n8n-automations",
        "labels": {
          "en": "N8n Automations",
          "it": "Automazioni N8n"
        },
        "path": "/docs/03-functional-analysis/n8n-automations"
      },
      {
        "slug": "user-interface",
        "labels": {
          "en": "User Interface",
          "it": "Interfaccia Utente"
        },
        "path": "/docs/03-functional-analysis/user-interface"
      },
      {
        "slug": "monitoring-dashboard",
        "labels": {
          "en": "Monitoring Dashboard",
          "it": "Dashboard Monitoraggio"
        },
        "path": "/docs/03-functional-analysis/monitoring-dashboard"
      }
    ]
  },
  {
    "id": "04-architecture",
    "labels": {
      "en": "Architecture",
      "it": "Architettura"
    },
    "position": 4,
    "docs": [
      {
        "slug": "general-architecture",
        "labels": {
          "en": "General Architecture",
          "it": "Architettura Generale"
        },
        "path": "/docs/04-architecture/general-architecture"
      },
      {
        "slug": "hexagonal-architecture",
        "labels": {
          "en": "Hexagonal Architecture",
          "it": "Architettura Esagonale"
        },
        "path": "/docs/04-architecture/hexagonal-architecture"
      },
      {
        "slug": "maven-modules",
        "labels": {
          "en": "Maven Modules",
          "it": "Moduli Maven"
        },
        "path": "/docs/04-architecture/maven-modules"
      },
      {
        "slug": "flow-diagrams",
        "labels": {
          "en": "Flow Diagrams",
          "it": "Diagrammi Flusso"
        },
        "path": "/docs/04-architecture/flow-diagrams"
      },
      {
        "slug": "rag-pipeline",
        "labels": {
          "en": "Rag Pipeline",
          "it": "Pipeline Rag"
        },
        "path": "/docs/04-architecture/rag-pipeline"
      }
    ]
  },
  {
    "id": "05-technology-stack",
    "labels": {
      "en": "Technology Stack",
      "it": "Stack Tecnologico"
    },
    "position": 5,
    "docs": [
      {
        "slug": "backend-stack",
        "labels": {
          "en": "Backend Stack",
          "it": "Backend Stack"
        },
        "path": "/docs/05-technology-stack/backend-stack"
      },
      {
        "slug": "frontend-stack",
        "labels": {
          "en": "Frontend Stack",
          "it": "Frontend Stack"
        },
        "path": "/docs/05-technology-stack/frontend-stack"
      },
      {
        "slug": "docker-infrastructure",
        "labels": {
          "en": "Docker Infrastructure",
          "it": "Infrastruttura Docker"
        },
        "path": "/docs/05-technology-stack/docker-infrastructure"
      },
      {
        "slug": "backend-testing",
        "labels": {
          "en": "Backend Testing",
          "it": "Test Backend"
        },
        "path": "/docs/05-technology-stack/backend-testing"
      },
      {
        "slug": "functional-testing",
        "labels": {
          "en": "Functional Testing",
          "it": "Test Funzionali"
        },
        "path": "/docs/05-technology-stack/functional-testing"
      }
    ]
  },
  {
    "id": "06-data-model",
    "labels": {
      "en": "Data Model",
      "it": "Modello Dati"
    },
    "position": 6,
    "docs": [
      {
        "slug": "database-schema",
        "labels": {
          "en": "Database Schema",
          "it": "Schema Database"
        },
        "path": "/docs/06-data-model/database-schema"
      },
      {
        "slug": "jpa-entities",
        "labels": {
          "en": "Jpa Entities",
          "it": "Entita Jpa"
        },
        "path": "/docs/06-data-model/jpa-entities"
      },
      {
        "slug": "flyway-migrations",
        "labels": {
          "en": "Flyway Migrations",
          "it": "Migrazioni Flyway"
        },
        "path": "/docs/06-data-model/flyway-migrations"
      }
    ]
  },
  {
    "id": "06-security",
    "labels": {
      "en": "Security",
      "it": "Sicurezza"
    },
    "position": 7,
    "docs": [
      {
        "slug": "local-authentication",
        "labels": {
          "en": "Local Authentication",
          "it": "Autenticazione Locale"
        },
        "path": "/docs/06-security/local-authentication"
      }
    ]
  },
  {
    "id": "07-api-reference",
    "labels": {
      "en": "Api Reference",
      "it": "Api Reference"
    },
    "position": 8,
    "docs": [
      {
        "slug": "api-overview",
        "labels": {
          "en": "Api Overview",
          "it": "Panoramica Api"
        },
        "path": "/docs/07-api-reference/api-overview"
      },
      {
        "slug": "chat-api",
        "labels": {
          "en": "Chat Api",
          "it": "Chat Api"
        },
        "path": "/docs/07-api-reference/chat-api"
      },
      {
        "slug": "documents-api",
        "labels": {
          "en": "Documents Api",
          "it": "Documents Api"
        },
        "path": "/docs/07-api-reference/documents-api"
      },
      {
        "slug": "models-api",
        "labels": {
          "en": "Models Api",
          "it": "Models Api"
        },
        "path": "/docs/07-api-reference/models-api"
      },
      {
        "slug": "dashboard-api",
        "labels": {
          "en": "Dashboard Api",
          "it": "Dashboard Api"
        },
        "path": "/docs/07-api-reference/dashboard-api"
      },
      {
        "slug": "webhooks-api",
        "labels": {
          "en": "Webhooks Api",
          "it": "Webhooks Api"
        },
        "path": "/docs/07-api-reference/webhooks-api"
      }
    ]
  },
  {
    "id": "08-frontend",
    "labels": {
      "en": "Frontend",
      "it": "Frontend"
    },
    "position": 9,
    "docs": [
      {
        "slug": "project-structure",
        "labels": {
          "en": "Project Structure",
          "it": "Struttura Progetto"
        },
        "path": "/docs/08-frontend/project-structure"
      },
      {
        "slug": "routing-lazy-loading",
        "labels": {
          "en": "Routing Lazy Loading",
          "it": "Routing Lazy Loading"
        },
        "path": "/docs/08-frontend/routing-lazy-loading"
      },
      {
        "slug": "state-management-signals",
        "labels": {
          "en": "State Management Signals",
          "it": "State Management Signals"
        },
        "path": "/docs/08-frontend/state-management-signals"
      },
      {
        "slug": "feature-modules",
        "labels": {
          "en": "Feature Modules",
          "it": "Feature Modules"
        },
        "path": "/docs/08-frontend/feature-modules"
      },
      {
        "slug": "e2e-testing-playwright",
        "labels": {
          "en": "E2e Testing Playwright",
          "it": "Test E2e Playwright"
        },
        "path": "/docs/08-frontend/e2e-testing-playwright"
      }
    ]
  },
  {
    "id": "09-security-privacy",
    "labels": {
      "en": "Security Privacy",
      "it": "Sicurezza Privacy"
    },
    "position": 10,
    "docs": [
      {
        "slug": "security-principles",
        "labels": {
          "en": "Security Principles",
          "it": "Principi Sicurezza"
        },
        "path": "/docs/09-security-privacy/security-principles"
      },
      {
        "slug": "spring-security-config",
        "labels": {
          "en": "Spring Security Config",
          "it": "Spring Security Config"
        },
        "path": "/docs/09-security-privacy/spring-security-config"
      },
      {
        "slug": "credential-management",
        "labels": {
          "en": "Credential Management",
          "it": "Gestione Credenziali"
        },
        "path": "/docs/09-security-privacy/credential-management"
      }
    ]
  },
  {
    "id": "10-deployment",
    "labels": {
      "en": "Deployment",
      "it": "Deployment"
    },
    "position": 11,
    "docs": [
      {
        "slug": "development-environment",
        "labels": {
          "en": "Development Environment",
          "it": "Ambiente Sviluppo"
        },
        "path": "/docs/10-deployment/development-environment"
      },
      {
        "slug": "production-environment",
        "labels": {
          "en": "Production Environment",
          "it": "Ambiente Produzione"
        },
        "path": "/docs/10-deployment/production-environment"
      },
      {
        "slug": "docker-compose",
        "labels": {
          "en": "Docker Compose",
          "it": "Docker Compose"
        },
        "path": "/docs/10-deployment/docker-compose"
      },
      {
        "slug": "installation-guide",
        "labels": {
          "en": "Installation Guide",
          "it": "Guida Installazione"
        },
        "path": "/docs/10-deployment/installation-guide"
      },
      {
        "slug": "startup-scripts",
        "labels": {
          "en": "Startup Scripts",
          "it": "Scripts Avvio"
        },
        "path": "/docs/10-deployment/startup-scripts"
      }
    ]
  },
  {
    "id": "11-roadmap",
    "labels": {
      "en": "Roadmap",
      "it": "Roadmap"
    },
    "position": 12,
    "docs": [
      {
        "slug": "development-phases",
        "labels": {
          "en": "Development Phases",
          "it": "Fasi Sviluppo"
        },
        "path": "/docs/11-roadmap/development-phases"
      },
      {
        "slug": "future-evolution",
        "labels": {
          "en": "Future Evolution",
          "it": "Evoluzione Futura"
        },
        "path": "/docs/11-roadmap/future-evolution"
      }
    ]
  },
  {
    "id": "12-mcp-integration",
    "labels": {
      "en": "Mcp Integration",
      "it": "Mcp Integration"
    },
    "position": 13,
    "docs": [
      {
        "slug": "mcp-protocol-overview",
        "labels": {
          "en": "Mcp Protocol Overview",
          "it": "Panoramica Protocollo Mcp"
        },
        "path": "/docs/12-mcp-integration/mcp-protocol-overview"
      },
      {
        "slug": "server-implementation",
        "labels": {
          "en": "Server Implementation",
          "it": "Server Implementation"
        },
        "path": "/docs/12-mcp-integration/server-implementation"
      },
      {
        "slug": "client-implementation",
        "labels": {
          "en": "Client Implementation",
          "it": "Client Implementation"
        },
        "path": "/docs/12-mcp-integration/client-implementation"
      },
      {
        "slug": "configuration",
        "labels": {
          "en": "Configuration",
          "it": "Configurazione"
        },
        "path": "/docs/12-mcp-integration/configuration"
      },
      {
        "slug": "usage-examples",
        "labels": {
          "en": "Usage Examples",
          "it": "Esempi Utilizzo"
        },
        "path": "/docs/12-mcp-integration/usage-examples"
      },
      {
        "slug": "agent-integration",
        "labels": {
          "en": "Agent Integration",
          "it": "Integrazione Agenti"
        },
        "path": "/docs/12-mcp-integration/agent-integration"
      },
      {
        "slug": "troubleshooting",
        "labels": {
          "en": "Troubleshooting",
          "it": "Troubleshooting"
        },
        "path": "/docs/12-mcp-integration/troubleshooting"
      }
    ]
  },
  {
    "id": "13-user-guide",
    "labels": {
      "en": "User Guide",
      "it": "Guida Utente"
    },
    "position": 14,
    "docs": [
      {
        "slug": "introduction",
        "labels": {
          "en": "Introduction",
          "it": "Introduzione"
        },
        "path": "/docs/13-user-guide/introduction"
      },
      {
        "slug": "dashboard",
        "labels": {
          "en": "Dashboard",
          "it": "Dashboard"
        },
        "path": "/docs/13-user-guide/dashboard"
      },
      {
        "slug": "chat",
        "labels": {
          "en": "Chat",
          "it": "Chat"
        },
        "path": "/docs/13-user-guide/chat"
      },
      {
        "slug": "documents",
        "labels": {
          "en": "Documents",
          "it": "Documenti"
        },
        "path": "/docs/13-user-guide/documents"
      },
      {
        "slug": "semantic-search",
        "labels": {
          "en": "Semantic Search",
          "it": "Ricerca Semantica"
        },
        "path": "/docs/13-user-guide/semantic-search"
      },
      {
        "slug": "monitored-folders",
        "labels": {
          "en": "Monitored Folders",
          "it": "Cartelle Monitorate"
        },
        "path": "/docs/13-user-guide/monitored-folders"
      },
      {
        "slug": "provider-settings",
        "labels": {
          "en": "Provider Settings",
          "it": "Impostazioni Provider"
        },
        "path": "/docs/13-user-guide/provider-settings"
      },
      {
        "slug": "mcp-integration",
        "labels": {
          "en": "Mcp Integration",
          "it": "Mcp Integration"
        },
        "path": "/docs/13-user-guide/mcp-integration"
      }
    ]
  },
  {
    "id": "15-extension-domains",
    "labels": {
      "en": "Extension Domains",
      "it": "Ambiti Estensione"
    },
    "position": 16,
    "docs": [
      {
        "slug": "tourism-territory",
        "labels": {
          "en": "Tourism Territory",
          "it": "Turismo Territorio"
        },
        "path": "/docs/15-extension-domains/tourism-territory"
      },
      {
        "slug": "events-entertainment",
        "labels": {
          "en": "Events Entertainment",
          "it": "Eventi Spettacoli"
        },
        "path": "/docs/15-extension-domains/events-entertainment"
      },
      {
        "slug": "food-venues",
        "labels": {
          "en": "Food Venues",
          "it": "Ristorazione Locali"
        },
        "path": "/docs/15-extension-domains/food-venues"
      },
      {
        "slug": "itineraries-experiences",
        "labels": {
          "en": "Itineraries Experiences",
          "it": "Itinerari Esperienze"
        },
        "path": "/docs/15-extension-domains/itineraries-experiences"
      },
      {
        "slug": "education-students",
        "labels": {
          "en": "Education Students",
          "it": "Education Studenti"
        },
        "path": "/docs/15-extension-domains/education-students"
      },
      {
        "slug": "culture-art-museums",
        "labels": {
          "en": "Culture Art Museums",
          "it": "Cultura Arte Musei"
        },
        "path": "/docs/15-extension-domains/culture-art-museums"
      },
      {
        "slug": "sports-outdoor",
        "labels": {
          "en": "Sports Outdoor",
          "it": "Sport Outdoor"
        },
        "path": "/docs/15-extension-domains/sports-outdoor"
      },
      {
        "slug": "local-commerce-shopping",
        "labels": {
          "en": "Local Commerce Shopping",
          "it": "Commercio Shopping Locale"
        },
        "path": "/docs/15-extension-domains/local-commerce-shopping"
      },
      {
        "slug": "real-estate",
        "labels": {
          "en": "Real Estate",
          "it": "Real Estate Immobiliare"
        },
        "path": "/docs/15-extension-domains/real-estate"
      },
      {
        "slug": "local-services-health",
        "labels": {
          "en": "Local Services Health",
          "it": "Servizi Sanita Locale"
        },
        "path": "/docs/15-extension-domains/local-services-health"
      },
      {
        "slug": "enterprise-knowledge-base",
        "labels": {
          "en": "Enterprise Knowledge Base",
          "it": "Knowledge Base Aziendale"
        },
        "path": "/docs/15-extension-domains/enterprise-knowledge-base"
      },
      {
        "slug": "software-architecture",
        "labels": {
          "en": "Software Architecture",
          "it": "Architettura Software"
        },
        "path": "/docs/15-extension-domains/software-architecture"
      },
      {
        "slug": "processes-workflows",
        "labels": {
          "en": "Processes Workflows",
          "it": "Processi Workflow"
        },
        "path": "/docs/15-extension-domains/processes-workflows"
      },
      {
        "slug": "people-skills",
        "labels": {
          "en": "People Skills",
          "it": "Persone Competenze"
        },
        "path": "/docs/15-extension-domains/people-skills"
      },
      {
        "slug": "customers-suppliers",
        "labels": {
          "en": "Customers Suppliers",
          "it": "Clienti Fornitori"
        },
        "path": "/docs/15-extension-domains/customers-suppliers"
      },
      {
        "slug": "email-communications",
        "labels": {
          "en": "Email Communications",
          "it": "Mail Comunicazioni"
        },
        "path": "/docs/15-extension-domains/email-communications"
      },
      {
        "slug": "ticketing-decisions",
        "labels": {
          "en": "Ticketing Decisions",
          "it": "Ticketing Decisioni"
        },
        "path": "/docs/15-extension-domains/ticketing-decisions"
      },
      {
        "slug": "onboarding-training",
        "labels": {
          "en": "Onboarding Training",
          "it": "Onboarding Formazione"
        },
        "path": "/docs/15-extension-domains/onboarding-training"
      },
      {
        "slug": "compliance-audit",
        "labels": {
          "en": "Compliance Audit",
          "it": "Compliance Audit"
        },
        "path": "/docs/15-extension-domains/compliance-audit"
      },
      {
        "slug": "knowledge-graph-engine",
        "labels": {
          "en": "Knowledge Graph Engine",
          "it": "Motore Knowledge Graph"
        },
        "path": "/docs/15-extension-domains/knowledge-graph-engine"
      },
      {
        "slug": "graphrag-ai",
        "labels": {
          "en": "Graphrag Ai",
          "it": "Graphrag Ai"
        },
        "path": "/docs/15-extension-domains/graphrag-ai"
      },
      {
        "slug": "graph-visualization",
        "labels": {
          "en": "Graph Visualization",
          "it": "Visualizzazione Grafo"
        },
        "path": "/docs/15-extension-domains/graph-visualization"
      },
      {
        "slug": "community-moderation",
        "labels": {
          "en": "Community Moderation",
          "it": "Community Moderazione"
        },
        "path": "/docs/15-extension-domains/community-moderation"
      },
      {
        "slug": "ingestion-connectors",
        "labels": {
          "en": "Ingestion Connectors",
          "it": "Ingestione Connettori"
        },
        "path": "/docs/15-extension-domains/ingestion-connectors"
      },
      {
        "slug": "security-privacy",
        "labels": {
          "en": "Security Privacy",
          "it": "Sicurezza Privacy"
        },
        "path": "/docs/15-extension-domains/security-privacy"
      }
    ]
  }
];
