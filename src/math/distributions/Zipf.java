/**
 * Amua - An open source modeling framework.
 * Copyright (C) 2017 Zachary J. Ward
 *
 * This file is part of Amua. Amua is free software: you can redistribute
 * it and/or modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * Amua is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Amua.  If not, see <http://www.gnu.org/licenses/>.
 */

package math.distributions;

import math.MathUtils;
import math.Numeric;
import math.NumericException;

import org.apache.commons.math3.distribution.ZipfDistribution;

import main.MersenneTwisterFast;

public final class Zipf{
	
	public static Numeric pmf(Numeric params[]) throws NumericException{
		if(params[0].isMatrix()==false && params[1].isMatrix()==false && params[2].isMatrix()==false) { //real number
			int k=params[0].getInt();
			double s=params[1].getDouble(); int n=params[2].getInt();
			if(s<=0){throw new NumericException("s should be >0","Zipf");}
			if(n<1){throw new NumericException("n should be ≥1","Zipf");}
			ZipfDistribution zipf=new ZipfDistribution(null,n,s);
			return(new Numeric(zipf.probability(k)));
		}
		else { //matrix
			if(params[0].nrow!=params[1].nrow || params[0].ncol!=params[1].ncol) {throw new NumericException("k and s should be the same size","Zipf");}
			if(params[1].nrow!=params[2].nrow || params[1].ncol!=params[2].ncol) {throw new NumericException("s and n should be the same size","Zipf");}
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					int k=(int)params[0].matrix[i][j];
					double s=params[1].matrix[i][j];
					int n=(int)params[2].matrix[i][j];
					if(s<=0){throw new NumericException("s should be >0","Zipf");}
					if(n<1){throw new NumericException("n should be ≥1","Zipf");}
					ZipfDistribution zipf=new ZipfDistribution(null,n,s);
					vals.matrix[i][j]=zipf.probability(k);
				}
			}
			return(vals);
		}
	}

	public static Numeric cdf(Numeric params[]) throws NumericException{
		if(params[0].isMatrix()==false && params[1].isMatrix()==false && params[2].isMatrix()==false) { //real number
			int k=params[0].getInt();
			double s=params[1].getDouble(); int n=params[2].getInt();
			if(s<=0){throw new NumericException("s should be >0","Zipf");}
			if(n<1){throw new NumericException("n should be ≥1","Zipf");}
			ZipfDistribution zipf=new ZipfDistribution(null,n,s);
			return(new Numeric(zipf.cumulativeProbability(k)));
		}
		else { //matrix
			if(params[0].nrow!=params[1].nrow || params[0].ncol!=params[1].ncol) {throw new NumericException("k and s should be the same size","Zipf");}
			if(params[1].nrow!=params[2].nrow || params[1].ncol!=params[2].ncol) {throw new NumericException("s and n should be the same size","Zipf");}
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					int k=(int)params[0].matrix[i][j];
					double s=params[1].matrix[i][j];
					int n=(int)params[2].matrix[i][j];
					if(s<=0){throw new NumericException("s should be >0","Zipf");}
					if(n<1){throw new NumericException("n should be ≥1","Zipf");}
					ZipfDistribution zipf=new ZipfDistribution(null,n,s);
					vals.matrix[i][j]=zipf.cumulativeProbability(k);
				}
			}
			return(vals);
		}
	}	
	
	public static Numeric quantile(Numeric params[]) throws NumericException{
		if(params[0].isMatrix()==false && params[1].isMatrix()==false && params[2].isMatrix()==false) { //real number
			double x=params[0].getProb();
			double s=params[1].getDouble(); int n=params[2].getInt();
			if(s<=0){throw new NumericException("s should be >0","Zipf");}
			if(n<1){throw new NumericException("n should be ≥1","Zipf");}
			ZipfDistribution zipf=new ZipfDistribution(null,n,s);
			return(new Numeric(zipf.inverseCumulativeProbability(x)));
		}
		else { //matrix
			if(params[0].nrow!=params[1].nrow || params[0].ncol!=params[1].ncol) {throw new NumericException("x and s should be the same size","Zipf");}
			if(params[1].nrow!=params[2].nrow || params[1].ncol!=params[2].ncol) {throw new NumericException("s and n should be the same size","Zipf");}
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					double x=params[0].getMatrixProb(i, j);
					double s=params[1].matrix[i][j];
					int n=(int)params[2].matrix[i][j];
					if(s<=0){throw new NumericException("s should be >0","Zipf");}
					if(n<1){throw new NumericException("n should be ≥1","Zipf");}
					ZipfDistribution zipf=new ZipfDistribution(null,n,s);
					vals.matrix[i][j]=zipf.inverseCumulativeProbability(x);
				}
			}
			return(vals);
		}
	}
	
	public static Numeric mean(Numeric params[]) throws NumericException{
		if(params[0].isMatrix()==false && params[1].isMatrix()==false) { //real number
			double s=params[0].getDouble(); int n=params[1].getInt();
			if(s<=0){throw new NumericException("s should be >0","Zipf");}
			if(n<1){throw new NumericException("n should be ≥1","Zipf");}
			ZipfDistribution zipf=new ZipfDistribution(null,n,s);
			return(new Numeric(zipf.getNumericalMean()));
		}
		else { //matrix
			if(params[0].nrow!=params[1].nrow || params[0].ncol!=params[1].ncol) {throw new NumericException("s and n should be the same size","Zipf");}
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					double s=params[0].matrix[i][j];
					int n=(int)params[1].matrix[i][j];
					if(s<=0){throw new NumericException("s should be >0","Zipf");}
					if(n<1){throw new NumericException("n should be ≥1","Zipf");}
					ZipfDistribution zipf=new ZipfDistribution(null,n,s);
					vals.matrix[i][j]=zipf.getNumericalMean();
				}
			}
			return(vals);
		}
	}
	
	public static Numeric variance(Numeric params[]) throws NumericException{
		if(params[0].isMatrix()==false && params[1].isMatrix()==false) { //real number
			double s=params[0].getDouble(); int n=params[1].getInt();
			if(s<=0){throw new NumericException("s should be >0","Zipf");}
			if(n<1){throw new NumericException("n should be ≥1","Zipf");}
			ZipfDistribution zipf=new ZipfDistribution(null,n,s);
			return(new Numeric(zipf.getNumericalVariance()));
		}
		else { //matrix
			if(params[0].nrow!=params[1].nrow || params[0].ncol!=params[1].ncol) {throw new NumericException("s and n should be the same size","Zipf");}
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					double s=params[0].matrix[i][j];
					int n=(int)params[1].matrix[i][j];
					if(s<=0){throw new NumericException("s should be >0","Zipf");}
					if(n<1){throw new NumericException("n should be ≥1","Zipf");}
					ZipfDistribution zipf=new ZipfDistribution(null,n,s);
					vals.matrix[i][j]=zipf.getNumericalVariance();
				}
			}
			return(vals);
		}
	}
	
	public static Numeric sample(Numeric params[], MersenneTwisterFast generator) throws NumericException{
		if(params.length!=2){
			throw new NumericException("Incorrect number of parameters","Zipf");
		}
		if(params[0].isMatrix()==false && params[1].isMatrix()==false) { //real number
			double s=params[0].getDouble(); int n=params[1].getInt();
			if(s<=0){throw new NumericException("s should be >0","Zipf");}
			if(n<1){throw new NumericException("n should be ≥1","Zipf");}
			ZipfDistribution zipf=new ZipfDistribution(null,n,s);
			double rand=generator.nextDouble();
			return(new Numeric(zipf.inverseCumulativeProbability(rand)));
		}
		else{ //matrix
			if(params[0].nrow!=params[1].nrow || params[0].ncol!=params[1].ncol) {throw new NumericException("s and n should be the same size","Zipf");}
			int nrow=params[0].nrow; int ncol=params[0].ncol;
			Numeric vals=new Numeric(nrow,ncol); //create result matrix
			for(int i=0; i<nrow; i++) {
				for(int j=0; j<ncol; j++) {
					double s=params[0].matrix[i][j];
					int n=(int)params[1].matrix[i][j];
					if(s<=0){throw new NumericException("s should be >0","Zipf");}
					if(n<1){throw new NumericException("n should be ≥1","Zipf");}
					ZipfDistribution zipf=new ZipfDistribution(null,n,s);
					double rand=generator.nextDouble();
					vals.matrix[i][j]=zipf.inverseCumulativeProbability(rand);
				}
			}
			return(vals);
		}
	}
	
	public static String description(){
		String des="<html><b>Zipf Distribution</b><br>";
		des+="Used to model discrete power law distributions<br>";
		des+="<br><i>Parameters</i><br>";
		des+=MathUtils.consoleFont("s")+": Exponent (real number "+MathUtils.consoleFont(">0")+") <br>";
		des+=MathUtils.consoleFont("n")+": Number of elements (integer "+MathUtils.consoleFont("≥1")+") <br>";
		des+="<br><i>Sample</i><br>";
		des+=MathUtils.consoleFont("<b>Zipf</b>","green")+MathUtils.consoleFont("(s,n,<b><i>~</i></b>)")+": Returns a random variable (mean in base case) from the Zipf distribution. Integer in "+MathUtils.consoleFont("{1,2,...,n}")+"<br>";
		des+="<br><i>Distribution Functions</i><br>";
		des+=MathUtils.consoleFont("<b>Zipf</b>","green")+MathUtils.consoleFont("(k,s,n,<b><i>f</i></b>)")+": Returns the value of the Zipf PMF at "+MathUtils.consoleFont("k")+"<br>";
		des+=MathUtils.consoleFont("<b>Zipf</b>","green")+MathUtils.consoleFont("(k,s,n,<b><i>F</i></b>)")+": Returns the value of the Zipf CDF at "+MathUtils.consoleFont("k")+"<br>";
		des+=MathUtils.consoleFont("<b>Zipf</b>","green")+MathUtils.consoleFont("(x,s,n,<b><i>Q</i></b>)")+": Returns the quantile (inverse CDF) of the Zipf distribution at "+MathUtils.consoleFont("x")+"<br>";
		des+="<br><i>Moments</i><br>";
		des+=MathUtils.consoleFont("<b>Zipf</b>","green")+MathUtils.consoleFont("(s,n,<b><i>E</i></b>)")+": Returns the mean of the Zipf distribution<br>";
		des+=MathUtils.consoleFont("<b>Zipf</b>","green")+MathUtils.consoleFont("(s,n,<b><i>V</i></b>)")+": Returns the variance of the Zipf distribution<br>";
		des+="</html>";
		return(des);
	}
}