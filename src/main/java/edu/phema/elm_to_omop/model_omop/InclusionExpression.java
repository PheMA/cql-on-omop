package edu.phema.elm_to_omop.model_omop;

public class InclusionExpression {

        private String type;
        private InclusionCriteriaList icl;
        private InclusionDemographic id;
        private InclusionGroups ig;
        
        public InclusionExpression(String type, InclusionCriteriaList icl, InclusionDemographic id, InclusionGroups ig) {
            super();
            this.type = type;
            this.icl = icl;
            this.id = id;
            this.ig = ig;
        }

        public String getType() {
            return type;
        }

        public InclusionCriteriaList getInclusionCriteriaList() {
            return icl;
        }

        public InclusionDemographic getInclusionDemographic() {
            return id;
        }

        public InclusionGroups getInclusionGroups() {
            return ig;
        }

        public void setType(String type) {
            this.type = type;
        }

        public void setInclusionCriteriaList(InclusionCriteriaList icl) {
            this.icl = icl;
        }

        public void setInclusionDemographic(InclusionDemographic id) {
            this.id = id;
        }

        public void setInclusionGroups(InclusionGroups ig) {
            this.ig = ig;
        }
        
        
        
}
