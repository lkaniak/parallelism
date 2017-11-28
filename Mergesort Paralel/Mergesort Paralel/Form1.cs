using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Threading;
using System.Windows.Forms;

namespace Mergesort_Paralel
{
    public partial class Form1 : Form
    {
        private List<int[]> numberListsSeq = new List<int[]>();
        private List<int[]> numberListsParalel = new List<int[]>();
        private int nThreads = Environment.ProcessorCount;
        private List<double> temposMergeSeq = new List<double>();
        private List<double> temposMergeParalel = new List<double>();
        private List<Thread> vecThreads;

        public Form1()
        {
            InitializeComponent();
            this.chart1.Series[0] = new System.Windows.Forms.DataVisualization.Charting.Series();
            this.chart1.ChartAreas[0].AxisX.Title = "TAMANHO DA SEQUÊNCIA";
            this.chart1.ChartAreas[0].AxisY.Title = "TEMPO DE EXECUÇÃO";
            this.chart1.Series[0].LegendText = "Sequencial";

            this.chart1.Series.Add(new System.Windows.Forms.DataVisualization.Charting.Series());
            this.chart1.ChartAreas.Add(new System.Windows.Forms.DataVisualization.Charting.ChartArea());
            this.chart1.ChartAreas[1].AxisX.Title = "TAMANHO DA PARALELO";
            this.chart1.ChartAreas[1].AxisY.Title = "TEMPO DE EXECUÇÃO";
            this.chart1.Series[1].LegendText = "Paralelo";
        }

        public void gerarVetoresClick(object sender, EventArgs e)
        {
            int N;
            int[] numeros = new int[0];
            this.numberListsSeq = new List<int[]>();
            this.numberListsParalel = new List<int[]>();

            long t0 = Environment.TickCount;
            for (int i = 15; i < 26; i++)
            {
                N = (int)Math.Pow(2, i);
                numeros = new int[N];
                this.gereNumeros(numeros);
            }

            double tFinal = (double)(Environment.TickCount - t0);
            tFinal = tFinal / 1000;

            this.numberListsParalel = this.numberListsSeq.ToList();

            MessageBox.Show(this, "Vetores iniciados com sucesso\nTempo: " + tFinal + " seg.", "Sucesso", MessageBoxButtons.OK);
        }

        public int getNumThreads()
        {
            return this.nThreads;
        }

        private void gereNumeros(int[] numeros)
        {
            Random random = new Random();

            for (int i = 0; i < numeros.Length; i++)
            {
                numeros[i] = random.Next(10 * numeros.Length);
            }

            this.numberListsSeq.Add(numeros.ToArray());
        }

        //Button Merge Paralelo
        private void button1_Click(object sender, EventArgs e)
        {
            this.chart1.Series[1] = new System.Windows.Forms.DataVisualization.Charting.Series();

            if (this.numberListsParalel.Count != 0)
            {
                //confere e ajusta numero de threads para 2^n processadores
                if (nThreads > 1)
                {
                    while (!IsPowerOfTwo(this.nThreads))
                    {
                        this.nThreads--;
                    }
                }
                else
                {
                    //throw erro (processadores insuficientes para paralelismo)
                }
                //inicializa vetor de threads
                this.vecThreads = new List<Thread>();
                //cria variaveis para os tempos
                long t0;
                double tFinal;
                this.temposMergeParalel = new List<double>();
                //efetua o mergesort Recursivo para cada lista
                int h = (int)Math.Log(this.nThreads, 2);
                SemaphoreSlim X = new SemaphoreSlim(0);
                for (int i = 0; i < this.numberListsParalel.Count; i++)
                {
                    t0 = Environment.TickCount;
                    this.mergeRec(this.numberListsParalel[i], 0, this.numberListsParalel[i].Length - 1, h, X);
                    X.Wait();
                    tFinal = (double)(Environment.TickCount - t0);
                    tFinal = tFinal / 1000;
                    this.temposMergeParalel.Add(tFinal);
                }

                this.plotGraphParalel();
            }
            else
            {
                MessageBox.Show(this, "Não existe nenhum vetor iniciado", "ERRO", MessageBoxButtons.OK);
            }
        }

        //Button Merge Sequencial
        private void button2_Click(object sender, EventArgs e)
        {
            this.chart1.Series[0] = new System.Windows.Forms.DataVisualization.Charting.Series();

            if (this.numberListsSeq.Count != 0)
            {
                long t0;
                double tFinal;
                this.temposMergeSeq = new List<double>();

                for (int i = 0; i < this.numberListsSeq.Count; i++)
                {
                    t0 = Environment.TickCount;
                    this.mergeSeq(this.numberListsSeq[i], 0, this.numberListsSeq[i].Length - 1);
                    tFinal = (double)(Environment.TickCount - t0);
                    tFinal = tFinal / 1000;
                    this.temposMergeSeq.Add(tFinal);
                }

                this.plotGraphSeq();
            }
            else
            {
                MessageBox.Show(this, "Não existe nenhum vetor iniciado", "ERRO", MessageBoxButtons.OK);
            }
        }

        private void mergeSeq(int[] numeros, int inicio, int fim)
        {
            int numElementos = fim - inicio + 1;
            if (numElementos > 2)
            {
                int meio = (inicio + fim) / 2;
                this.mergeSeq(numeros, inicio, meio);
                this.mergeSeq(numeros, meio + 1, fim);
                merge(numeros, numElementos, inicio, meio, fim);
            }
            else
            {
                if ((numElementos == 2) && (numeros[inicio] > numeros[fim]))
                {
                    int temp = numeros[inicio];
                    numeros[inicio] = numeros[fim];
                    numeros[fim] = temp;
                }
            }
        }

        private void mergeRec(int[] numeros, int inicio, int fim, int altura, SemaphoreSlim sm)
        {
            int m = (inicio + fim) / 2;
            int nElementos = fim - inicio + 1;

            if (altura == 0)
            {
                mergeSeq(numeros, inicio, fim);
            }
            else
            {
                SemaphoreSlim A = new SemaphoreSlim(0);
                SemaphoreSlim B = new SemaphoreSlim(0);

                Thread t1 = new Thread(() => mergeRec(numeros, inicio, m, altura - 1, A));
                t1.Start();
                
                Thread t2 = new Thread(() => mergeRec(numeros, m + 1, fim, altura - 1, B));
                t2.Start();

                A.Wait();               
                B.Wait();

                merge(numeros, nElementos, inicio, m, fim);

            }
            sm.Release();
        }

        private void merge(int[] numeros, int numElementos, int inicio, int meio, int fim)
        {
            int[] merged = new int[numElementos];
            int i = inicio;
            int j = meio + 1;
            int k = 0;

            while (i <= meio && j <= fim)
            {
                if (numeros[i] < numeros[j])
                {

                    merged[k] = numeros[i];
                    i++;
                }
                else
                {
                    merged[k] = numeros[j];
                    j++;
                }
                k++;
            }

            while (i <= meio)
            {
                merged[k] = numeros[i];
                i++;
                k++;
            }

            while (j <= fim)
            {
                merged[k] = numeros[j];
                j++;
                k++;
            }

            int s = inicio;
            for (int r = 0; r < numElementos; r++)
            {
                numeros[s] = merged[r];
                s++;
            }
            //return numeros;
            //return new int[];
        }

        private bool IsPowerOfTwo(int x)
        {
            return (x != 0) && ((x & (x - 1)) == 0);
        }

        private void plotGraphSeq()
        {
            this.chart1.Series[0].ChartType = System.Windows.Forms.DataVisualization.Charting.SeriesChartType.Line;
            this.chart1.Series[0].MarkerStyle = System.Windows.Forms.DataVisualization.Charting.MarkerStyle.Circle;
            this.chart1.Series[0].MarkerSize = 12;

            this.chart1.Series[0].Points.SuspendUpdates();

            for (int i = 0; i < this.numberListsSeq.Count; i++)
            {
                this.chart1.Series[0].Points.AddXY(this.numberListsSeq[i].Length, this.temposMergeSeq[i]);
                this.chart1.Series[0].Points[i].ToolTip = "Tempo: " + this.temposMergeSeq[i].ToString() +
                    " seg\nTamanho: " + this.numberListsSeq[i].Length.ToString();
            }

            this.chart1.Series[0].Points.ResumeUpdates();
        }

        private void plotGraphParalel()
        {
            this.chart1.Series[1].ChartType = System.Windows.Forms.DataVisualization.Charting.SeriesChartType.Line;
            this.chart1.Series[1].MarkerStyle = System.Windows.Forms.DataVisualization.Charting.MarkerStyle.Circle;
            this.chart1.Series[1].MarkerSize = 12;

            this.chart1.Series[1].Points.SuspendUpdates();

            for (int i = 0; i < this.numberListsParalel.Count; i++)
            {
                this.chart1.Series[1].Points.AddXY(this.numberListsParalel[i].Length, this.temposMergeParalel[i]);
                this.chart1.Series[1].Points[i].ToolTip = "Tempo: " + this.temposMergeParalel[i].ToString() +
                    " seg\nTamanho: " + this.numberListsParalel[i].Length.ToString();
            }

            this.chart1.Series[1].Points.ResumeUpdates();
        }
    }
}
