import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import {
  FineTuningJob,
  TrainingDataset,
  CreateDatasetRequest,
  StartTrainingRequest
} from '../models/finetuning.model';

@Injectable({ providedIn: 'root' })
export class FineTuningService {
  private readonly baseUrl = `${environment.apiUrl}/finetuning`;

  constructor(private http: HttpClient) {}

  createDataset(request: CreateDatasetRequest): Observable<TrainingDataset> {
    return this.http.post<TrainingDataset>(`${this.baseUrl}/datasets`, request);
  }

  listDatasets(): Observable<TrainingDataset[]> {
    return this.http.get<TrainingDataset[]>(`${this.baseUrl}/datasets`);
  }

  startTraining(request: StartTrainingRequest): Observable<FineTuningJob> {
    return this.http.post<FineTuningJob>(`${this.baseUrl}/jobs`, request);
  }

  listJobs(): Observable<FineTuningJob[]> {
    return this.http.get<FineTuningJob[]>(`${this.baseUrl}/jobs`);
  }

  getJob(jobId: string): Observable<FineTuningJob> {
    return this.http.get<FineTuningJob>(`${this.baseUrl}/jobs/${jobId}`);
  }

  deployModel(jobId: string): Observable<void> {
    return this.http.post<void>(`${this.baseUrl}/jobs/${jobId}/deploy`, {});
  }
}
