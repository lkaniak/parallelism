//Projeto: Multiplicador Paralelo de Matrizes
//Disciplina: Sistemas Concorrentes - PUCPR
//Alunos: Bruno Cattalini, Lucas Kaniak e Felipe Mathieu
//Setembro, 2017

//Classe elemento(multiplicador, multiplicando e resultado)
 
public class elemento

{
  private int lA[];
  private int cB[];
  private int m;
  private int n;
  private int res;
  
  
  public elemento(int[] linhaA, int[] colunaB, int lin, int col)
  {
	this.lA = new int[linhaA.length];
	this.cB = new int[colunaB.length];
	this.lA = linhaA;
	this.cB = colunaB;
	this.m = lin;
	this.n = col;
  }
  
  public int get_resultado()
  {
  	return this.res;
  }

  public void set_resultado()
  {
	  this.res = 0;
	for(int i = 0; i < this.lA.length; i++)
	{
		this.res = this.res + lA[i]*cB[i];
	}
  }

  public int get_m()
  {
	  return this.m;
  }
  
  public int get_n()
  {
	  return this.n;
  }
  
  public void print_elemento(int num)
  {
    System.out.printf("Elemento %d:\n", num);
    System.out.printf("Linha %d:\n", this.m);
    for(int i = 0; i < this.lA.length; i++)
    {
      System.out.printf("[%d] ",this.lA[i]);
      System.out.println("");
    }
    System.out.printf("Coluna %d:\n", this.n);
    for(int i = 0; i < this.cB.length; i++)
    {
      System.out.printf("[%d] ",this.cB[i]);
      System.out.println("");
    }
  }
}
