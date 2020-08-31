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

package markov;

import base.AmuaModel;
import main.Variable;
import math.Interpreter;
import math.MathUtils;
import math.Numeric;
import math.NumericException;

public class MarkovCohort{
	MarkovNode chainRoot;
	MarkovTree markovTree;
	int numStates;
	MarkovNode states[];
	double curPrev[], newPrev[];
	int numDim;
	double cycleRewards[],cumRewards[];
	double cycleRewardsDis[],cumRewardsDis[];
	int numVariables;
	Variable variables[];
	double cycleVariables[];
	MarkovTrace trace;
	Variable curT;
	AmuaModel myModel;
	int curThread;
	
	//Constructor
	public MarkovCohort(MarkovNode chainRoot, int curThread){
		this.chainRoot=chainRoot;
		this.markovTree=chainRoot.tree;
		this.myModel=chainRoot.myModel;
		this.curThread=curThread;
		//Get Markov States
		numStates=chainRoot.stateNames.size();
		states=new MarkovNode[numStates];
		for(int s=0; s<numStates; s++){ //get pointers
			int index=chainRoot.childIndices.get(s);
			states[s]=markovTree.nodes.get(index);
		}
		curPrev=new double[numStates];
		newPrev=new double[numStates];
		numDim=chainRoot.numDimensions;
		cycleRewards=new double[numDim]; cycleRewardsDis=new double[numDim];
		cumRewards=new double[numDim]; cumRewardsDis=new double[numDim];
		numVariables=myModel.variables.size();
		cycleVariables=new double[numVariables];
		variables=new Variable[numVariables];
		for(int c=0; c<numVariables; c++){ //get pointers
			variables[c]=myModel.variables.get(c);
		}
		trace=new MarkovTrace(chainRoot);
		myModel.traceMarkov=trace;
		//Get state indices for all transition nodes
		chainRoot.transFrom=-1;
		getTransitionIndex(chainRoot);
	}
	
	public void simulate() throws NumericException, Exception{
		//Get innate variable 't'
		int indexT=myModel.getInnateVariableIndex("t");
		curT=myModel.innateVariables.get(indexT);
		curT.value[curThread]=new Numeric(0);
		curT.locked[curThread]=true;

		//Initialize variables
		myModel.unlockVarsAll(curThread);
		for(int c=0; c<numVariables; c++){
			variables[c].value[curThread]=Interpreter.evaluateTokens(variables[c].parsedTokens, curThread, false);
			variables[c].locked[curThread]=true;
		}
		
		//Perform Markov Chain variable updates for t=0
		if(chainRoot.hasVarUpdates && chainRoot.curVariableUpdatesT0!=null){
			//Perform variable updates
			for(int u=0; u<chainRoot.curVariableUpdatesT0.length; u++){
				chainRoot.curVariableUpdatesT0[u].update(false,curThread);
			}
			//Update any dependent variables
			for(int u=0; u<chainRoot.curVariableUpdatesT0.length; u++){
				chainRoot.curVariableUpdatesT0[u].variable.updateDependents(myModel,curThread);
			}
		}
		
		//Calculate initial state prevalence
		double sumProb=0;
		int indexCompProb=-1;
		for(int s=0; s<numStates; s++){
			if(states[s].prob.matches("C") || states[s].prob.matches("c")){ //Complementary
				states[s].curProb[0]=-1;
				indexCompProb=s;
			}
			else{ //Evaluate text
				states[s].curProb[0]=Interpreter.evaluateTokens(states[s].curProbTokens, curThread, false).getDouble();
				sumProb+=states[s].curProb[0];
			}
		}
		if(indexCompProb==-1){
			if(Math.abs(1.0-sumProb)>MathUtils.tolerance){ //throw error
				throw new Exception("Error: Probabilities sum to "+sumProb+" ("+chainRoot.name+")");
			}
		}
		else{
			if(sumProb>1.0 || sumProb<0.0){ //throw error
				throw new Exception("Error: Probabilities sum to "+sumProb+" ("+chainRoot.name+")");
			}
			else{
				states[indexCompProb].curProb[0]=1.0-sumProb;
			}
		}
		
		//Initialize state prevalence
		for(int s=0; s<numStates; s++){
			newPrev[s]=myModel.cohortSize*states[s].curProb[0];
			curPrev[s]=myModel.cohortSize*states[s].curProb[0];
		}
		
		//Simulate cycles
		int t=0;
		
		boolean terminate=false;
		while(terminate==false && t<markovTree.maxCycles){
			//update time dependent variables
			if(t>0) {
				curT.unlockDependents(curThread);
				curT.updateDependents(myModel, curThread);
			}
			
			//chain root variable updates
			if(t>0 && chainRoot.hasVarUpdates && chainRoot.curVariableUpdates!=null){
				//myModel.unlockVars(curThread);
				//Perform variable updates
				for(int u=0; u<chainRoot.curVariableUpdates.length; u++){
					chainRoot.curVariableUpdates[u].update(false,curThread);
				}
				//Update any dependent variables
				for(int u=0; u<chainRoot.curVariableUpdates.length; u++){
					chainRoot.curVariableUpdates[u].variable.updateDependents(myModel,curThread);
				}
			}
			
			for(int s=0; s<numStates; s++){ //Update each state
				for(int d=0; d<numDim; d++){ //Update state rewards
					double curReward=Interpreter.evaluateTokens(states[s].curRewardTokens[d], curThread, false).getDouble();
					cycleRewards[d]+=curReward*curPrev[s];
				}
				traverseNode(states[s],curPrev[s]);
			}
			updateTrace(t);
			terminate=checkTerminationCondition(); //check condition
			if(terminate && markovTree.halfCycleCorrection==true){
				trace.updateHalfCycle();
				//adjust cum rewards
				for(int d=0; d<numDim; d++){
					cumRewards[d]=trace.cumRewards[d].get(t);
					if(markovTree.discountRewards){
						cumRewardsDis[d]=trace.cumRewardsDis[d].get(t);
					}
				}
			}

			t++; //next cycle
			curT.value[curThread].setInt(t);
		}

		//Get chain EVs
		chainRoot.expectedValues=new double[numDim];
		chainRoot.expectedValuesDis=new double[numDim];
		for(int d=0; d<numDim; d++){
			chainRoot.expectedValues[d]=cumRewards[d];
			chainRoot.expectedValuesDis[d]=cumRewardsDis[d];
		}
		if(chainRoot.hasCost) {
			for(int d=0; d<numDim; d++){
				double curCost=Interpreter.evaluateTokens(chainRoot.curCostTokens[d],curThread,false).getDouble();
				curCost*=myModel.cohortSize;
				chainRoot.expectedValues[d]+=curCost;
				chainRoot.expectedValuesDis[d]+=curCost;
			}
		}

		//Reset variable 't'
		curT.value[curThread].setInt(0);
		
	}
	
	private boolean checkTerminationCondition(){
		boolean terminate=false;
		try{
			Numeric check=Interpreter.evaluateTokens(chainRoot.curTerminationTokens, curThread, false);
			if(check.getBool()){ //termination condition true
				terminate=true;
			}
		}catch(Exception e){
			e.printStackTrace();
			chainRoot.panel.errorLog.recordError(e);
			curT.value[curThread].setInt(0);
		}
		return(terminate);
	}
	
	/**
	 * Recursively traverse tree
	 * @throws Exception 
	 */
	
	private void traverseNode(MarkovNode node, double parentPrev) throws Exception{
		//Update prev at current node
		double nodePrev=parentPrev;
		if(node.type!=2){ //not state, update prev
			nodePrev=parentPrev*node.curProb[0];
		}
		
		//Update costs
		if(node.hasCost){
			for(int d=0; d<numDim; d++){
				double curCost=Interpreter.evaluateTokens(node.curCostTokens[d],curThread,false).getDouble();
				cycleRewards[d]+=curCost*nodePrev;
			}
		}
		//Update variables
		if(node.hasVarUpdates){
			//myModel.unlockVars(curThread);
			//Perform variable updates
			for(int u=0; u<node.curVariableUpdates.length; u++){
				node.curVariableUpdates[u].update(false,curThread);
			}
			//Update any dependent variables
			for(int u=0; u<node.curVariableUpdates.length; u++){
				node.curVariableUpdates[u].variable.updateDependents(myModel,curThread);
			}
		}
		
		//Calculate probabilities for children
		if(node.numChildren>0){
			double sumProb=0;
			int indexCompProb=-1;
			for(int c=0; c<node.numChildren; c++){
				MarkovNode curChild=node.children[c];
				if(curChild.prob.matches("C") || curChild.prob.matches("c")){ //Complementary
					curChild.curProb[0]=-1;
					indexCompProb=c;
				}
				else{ //Evaluate text
					curChild.curProb[0]=Interpreter.evaluateTokens(curChild.curProbTokens, curThread, false).getDouble();
					sumProb+=curChild.curProb[0];
				}
			}
			if(indexCompProb==-1){
				if(Math.abs(1.0-sumProb)>MathUtils.tolerance){ //throw error
					throw new Exception("Error: Probabilities sum to "+sumProb+" ("+node.chain.name+": "+node.name+")");
				}
			}
			else{
				if(sumProb>1.0 || sumProb<0.0){ //throw error
					throw new Exception("Error: Probabilities sum to "+sumProb+" ("+node.chain.name+": "+node.name+")");
				}
				else{
					MarkovNode curChild=node.children[indexCompProb];
					curChild.curProb[0]=1.0-sumProb;
				}
			}
		}
		
		
		if(node.type==4){ //Transition node, end of branch
			newPrev[node.transFrom]-=nodePrev; //from state
			newPrev[node.transTo]+=nodePrev; //next state
		}
		else{
			for(int c=0; c<node.numChildren; c++){
				MarkovNode curChild=node.children[c];
				traverseNode(curChild,nodePrev);
			}
		}
	}
	
	
	private void updateTrace(int t) throws NumericException{
		trace.cycles.add(t);
		//Update prev
		for(int s=0; s<numStates; s++){
			trace.prev[s].add(curPrev[s]); //prev at beginning of cycle
			curPrev[s]=newPrev[s];
		}
		//Check for half-cycle correction - first cycle
		if(t==0 && markovTree.halfCycleCorrection==true){
			for(int d=0; d<numDim; d++){
				cycleRewards[d]*=0.5; //half-cycle correction
			}
		}
		//Update rewards
		for(int d=0; d<numDim; d++){
			cumRewards[d]+=cycleRewards[d];
			trace.cycleRewards[d].add(cycleRewards[d]);
			trace.cumRewards[d].add(cumRewards[d]);
			if(markovTree.discountRewards){
				double discountRate=markovTree.discountRates[d]/100.0;
				double discountFactor=1.0;
				if(t<markovTree.discountStartCycle) { //don't discount yet
					discountFactor=1.0;
				}
				else { //discount
					int disCycle=(t-markovTree.discountStartCycle)+1;
					double disYear=disCycle/markovTree.cyclesPerYear; //convert to years
					discountFactor=1.0/Math.pow(1+discountRate, disYear);
				}
				
				cycleRewardsDis[d]=cycleRewards[d]*discountFactor;
				cumRewardsDis[d]+=cycleRewardsDis[d];
				trace.cycleRewardsDis[d].add(cycleRewardsDis[d]);
				trace.cumRewardsDis[d].add(cumRewardsDis[d]);
			}
			//reset
			cycleRewards[d]=0; 
			cycleRewardsDis[d]=0;
		}
		//Update variables
		for(int c=0; c<numVariables; c++){
			cycleVariables[c]=variables[c].value[curThread].getDouble();
			trace.cycleVariables[c].add(cycleVariables[c]);
		}
		
		trace.updateTable(t);
	}
	
	private void getTransitionIndex(MarkovNode node){
		if(node.type==4){ //get transition to
			//String nextState=(String) node.comboTransition.getSelectedItem();
			String nextState=node.transition;
			node.transTo=getStateIndex(nextState);
		}
		else{
			if(node.type==2){ //state, get transition from
				node.transFrom=getStateIndex(node.name);
			}
			for(int c=0; c<node.numChildren; c++){
				MarkovNode curChild=node.children[c];
				curChild.transFrom=node.transFrom; //pass to child
				getTransitionIndex(curChild);
			}
		}
	}
	
	private int getStateIndex(String name){
		int index=-1;
		boolean found=false;
		while(found==false){
			index++;
			if(states[index].name.equals(name)){
				found=true;
			}
		}
		return(index);
	}
}