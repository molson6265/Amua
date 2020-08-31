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

package cluster;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="ClusterInputs")
public class ClusterInputs{
	
	
	@XmlElement public String operation;
	@XmlElement public boolean seedIterationRNG;
	
	//psa inputs
	@XmlElement public boolean seedParamRNG;
	@XmlElement public int paramSeed;
	@XmlElement public boolean sampleParamSets;
	
		
	//Constructor
	public ClusterInputs(){
		
	}
		
	

}