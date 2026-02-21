export interface FineTuningJob {
  id: string;
  baseModel: string;
  technique: 'LORA' | 'QLORA' | 'FULL';
  status: 'PENDING' | 'PREPARING' | 'TRAINING' | 'COMPLETED' | 'FAILED' | 'DEPLOYED';
  progress: number;
  datasetId: string;
  outputModelName: string | null;
  hyperparameters: Record<string, string>;
  startedAt: string | null;
  completedAt: string | null;
  error: string | null;
  createdAt: string;
}

export interface TrainingDataset {
  id: string;
  name: string;
  documentIds: string[];
  sampleCount: number;
  format: string;
  createdAt: string;
}

export interface CreateDatasetRequest {
  name: string;
  documentIds: string[];
}

export interface StartTrainingRequest {
  datasetId: string;
  baseModel: string;
  technique: string;
  hyperparameters?: Record<string, string>;
}
