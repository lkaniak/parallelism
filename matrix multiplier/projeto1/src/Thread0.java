//Projeto: Multiplicador Paralelo de Matrizes
//Disciplina: Sistemas Concorrentes - PUCPR
//Alunos: Bruno Cattalini, Lucas Kaniak e Felipe Mathieu
//Setembro, 2017

//Thread Principal

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.concurrent.Semaphore;
import java.util.Vector;
import java.util.Random;

public class Thread0
{
  public static void main(String[] args)
  {
	
	/* declaracao de variaveis locais*/
	  
	//num_processadores
	int p = Runtime.getRuntime().availableProcessors();		
	//int p = 7;
	//Scanner in = new Scanner(System.in);
	//contador da barreira
	Contador contador = new Contador(0);
	//semaforo do tipo barreira para sincronizar as threads que calculam
    Semaphore barreira = new Semaphore(0);	
    //semaforo do tipo mutex para evitar race condition
    Semaphore mutex = new Semaphore(1);	
    //array dinamico contendo os elementos a serem calculados
    Vector<elemento> elementos = new Vector<elemento>(5,1); 
    //contador de arquivos
    int contArquivos = 0;
    //path do diretorio para salvar os arquivos
    String pathDir = ".\\";
    //matriz A
    int [][] A;
    //matriz B;
    int [][] B;
    //elemento "m" (X[m,n])
    int m = 0;
    //elemento "n" (X[m,n])
    int n = 0;
    //n de elementos a serem calculados por thread aprox.
    int n_elementos = 0;
    //matriz resposta (sempre mxn por definicao)
    int [][] C;
    //resto da divisao de n_elementos
    int resto = 0;
    //contador de elementos<> por iteracao
    int cont = 0;
    //contador de threads
    int k = 0;
    //vetor de threads
    Thread1 [] threads = new Thread1[p];
    
    //variaveis para fins de validacao
    int maxM = 0;
    int maxN = 0;
    int maxV = 0;
    
    
    /* inicio do algoritmo */

    
	 //while(true)
	 //{
		//atribuicoes
		Random gerador = new Random();
		maxV = gerador.nextInt(10);
		maxM = gerador.nextInt(10);
		maxN = gerador.nextInt(10);
		if(maxV == 0)
		{
			maxV = 1;
		}
		if(maxM == 0)
		{
			maxM = 1;
		}
		if(maxN == 0)
		{
			maxN = 1;
		}
		m = maxM;
		n = maxN;
	    n_elementos = (m*n)/p;
	    A = gerar_matriz_aleatoria(m,maxV);
	    B = gerar_matriz_aleatoria(maxV,n);
	    C = new int[m][n];
	    resto = (m*n) % p;
	    cont = 0;
	    k = 0;
	    
	    for(int i = 0; i < m; i++)
	    {
	    	for(int j = 0; j < n; j++)
	    	{
	    	//adicionar os elementos no vector baseado no n_elementos
	        //elemento(int[] linhaA, int[] colunaB, int lin, int col)
	        elementos.addElement(new elemento(A[i],get_coluna(B,j),i,j));
	        cont++;
	        if (cont == n_elementos) 
	        {
	          if (resto != 0) 
	          {
	        //adicionar um elemento a mais por conta do resto na thread
	            j++;
	            if (j >= n) 
	            {
	              i++;
	              if (i >= n) {break;}
	              else {j = 0;}
	            }
	            elementos.addElement(new elemento(A[i],get_coluna(B,j),i,j));
	        //diminuir o valor do resto em 1
	            resto--;
	          }
	        //disparar a thread
	          threads[k] = new Thread1(barreira,mutex,contador,elementos,n);
	          k++;
	          /*
	          for(int k = 0; k < elementos.size(); k++)
	          {
	          	elementos.get(k).print_elemento(k);
	          }
	          */
	        //resetar o vector elemento
	          cont = 0;
	          elementos.clear(); 
	        }		
	    		
	    	}
	    }
	    //controlar threads
	    for(int i = 0; i < p; i++){
	        threads[i].start();
	    }
	    try {
	    	barreira.acquire(p);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    for(int i = 0; i < p; i++)
	    {
	    	for(int j = 0; j < threads[i].respostas.size(); j++)
	    	{
	    		C[threads[i].respostas.get(j).get_m()][threads[i].respostas.get(j).get_n()] = threads[i].respostas.get(j).get_resultado();
	    	}
	    }
	    
	    
	    
	    //print resposta
	    for(int i = 0; i < m; i++)
	    {
	    	for(int j = 0; j < n; j++)
	    	{
	    		System.out.printf("[%d] ", C[i][j]);
	    	}
	    	System.out.println("");
	    }
	    contArquivos++;
	    gerar_arquivo(contArquivos,'A',pathDir,A);
	    gerar_arquivo(contArquivos,'B',pathDir,B);
	    gerar_arquivo(contArquivos,'C',pathDir,C);
	    
	 //}   

    
  }

  public static int[] get_coluna(int[][] M,int coln)
  {
    int [] coluna = new int[M.length];
    for (int i = 0; i < M.length ; i++) {
        coluna[i] = M[i][coln];
    }
    return coluna;
  }
  
  public static int[][] gerar_matriz_aleatoria(int x,int y)
  {
    int [][] M = new int[x][y];
    Random gerador = new Random();
    int negativo;
    for (int i = 0; i < x ; i++) 
    {
    	for(int j = 0; j < y; j++)
    	{
    		negativo = gerador.nextInt(2);
    		if(negativo == 0)
    		{
    			M[i][j] = gerador.nextInt(100);
    		}
    		else
    		{
    			M[i][j] = gerador.nextInt(100) * -1;
    		}
    		
    	}

    }
    return M;
  }
  public static void gerar_arquivo(int numArquivo, char matrizNome, String path, int [][] matriz)
  {
	  String strI = Integer.toString(numArquivo);
	  
	  String name =  (matrizNome + strI + ".txt");
	  int col = matriz.length;
	  int lin = matriz[0].length;
	  //NOVO ARQUIVO
	  try {
          //Whatever the file path is.
          File statText = new File(path+name);
          FileOutputStream is = new FileOutputStream(statText);
          OutputStreamWriter osw = new OutputStreamWriter(is);    
          Writer w = new BufferedWriter(osw);
          for (int i = 0; i < col ; i++) 
          {
          	for(int j = 0; j < lin; j++)
          	{
          		w.write("["+ matriz[i][j] + "]\t\t"); 
          	}
          	w.write("\r");
          	w.write("\n");
          }
          w.close();
      } catch (IOException e) {
    	  
      }
  }
  
}
