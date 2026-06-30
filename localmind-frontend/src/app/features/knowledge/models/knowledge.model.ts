export interface KnowledgeEntity {
  id: string;
  name: string;
  type: string;
  properties: Record<string, string>;
}

export interface KnowledgeRelation {
  id: string;
  fromEntityId: string;
  toEntityId: string;
  type: string;
  properties: Record<string, string>;
}

export interface KnowledgeSubgraph {
  entities: KnowledgeEntity[];
  relations: KnowledgeRelation[];
}

export interface KnowledgeGraphStats {
  totalEntities: number;
  totalRelations: number;
  entitiesByType: Record<string, number>;
}
