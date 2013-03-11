
package h225;
//
// This file was generated by the BinaryNotes compiler.
// See http://bnotes.sourceforge.net 
// Any modifications to this file will be lost upon recompilation of the source ASN.1. 
//

import org.bn.*;
import org.bn.annotations.*;
import org.bn.annotations.constraints.*;
import org.bn.coders.*;
import org.bn.types.*;




    @ASN1PreparedElement
    @ASN1Sequence ( name = "H221NonStandard", isSet = false )
    public class H221NonStandard implements IASN1PreparedElement {
            @ASN1Integer( name = "" )
    @ASN1ValueRangeConstraint ( 
		
		min = 0L, 
		
		max = 255L 
		
	   )
	   
        @ASN1Element ( name = "t35CountryCode", isOptional =  false , hasTag =  false  , hasDefaultValue =  false  )
    
	private Integer t35CountryCode = null;
                
  @ASN1Integer( name = "" )
    @ASN1ValueRangeConstraint ( 
		
		min = 0L, 
		
		max = 255L 
		
	   )
	   
        @ASN1Element ( name = "t35Extension", isOptional =  false , hasTag =  false  , hasDefaultValue =  false  )
    
	private Integer t35Extension = null;
                
  @ASN1Integer( name = "" )
    @ASN1ValueRangeConstraint ( 
		
		min = 0L, 
		
		max = 65535L 
		
	   )
	   
        @ASN1Element ( name = "manufacturerCode", isOptional =  false , hasTag =  false  , hasDefaultValue =  false  )
    
	private Integer manufacturerCode = null;
                
  
        
        public Integer getT35CountryCode () {
            return this.t35CountryCode;
        }

        

        public void setT35CountryCode (Integer value) {
            this.t35CountryCode = value;
        }
        
  
        
        public Integer getT35Extension () {
            return this.t35Extension;
        }

        

        public void setT35Extension (Integer value) {
            this.t35Extension = value;
        }
        
  
        
        public Integer getManufacturerCode () {
            return this.manufacturerCode;
        }

        

        public void setManufacturerCode (Integer value) {
            this.manufacturerCode = value;
        }
        
  
                    
        
        public void initWithDefaults() {
            
        }

        private static IASN1PreparedElementData preparedData = CoderFactory.getInstance().newPreparedElementData(H221NonStandard.class);
        public IASN1PreparedElementData getPreparedData() {
            return preparedData;
        }

            
    }
            