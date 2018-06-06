package org.terrier.querying;

import gnu.trove.TIntArrayList;

import org.terrier.matching.MatchingQueryTerms;
import org.terrier.matching.MatchingQueryTerms.MatchingTerm;
import org.terrier.matching.matchops.MultiTermOp;
import org.terrier.matching.matchops.Operator;
import org.terrier.matching.matchops.SingleTermOp;
import org.terrier.terms.BaseTermPipelineAccessor;
import org.terrier.terms.TermPipelineAccessor;
import org.terrier.utility.ApplicationSetup;

@ProcessPhaseRequisites(ManagerRequisite.MQT)
public class ApplyTermPipeline implements Process {

	TermPipelineAccessor tpa = null;
	String info = null;
	
	public ApplyTermPipeline()
	{
		this.load_pipeline();
	}
	
	/** load in the term pipeline */
	protected void load_pipeline()
	{
		final String[] pipes = ApplicationSetup.getProperty(
				info = "termpipelines", "Stopwords,PorterStemmer").trim()
				.split("\\s*,\\s*");
		synchronized (this) {
			tpa = new BaseTermPipelineAccessor(pipes);
		}		
	}
	
	interface Visitor {
		boolean visit(Operator qt);
		boolean visit(SingleTermOp sqt);
		boolean visit(MultiTermOp mqt);
	}
	
	
	
	@Override
	public void process(Manager manager, Request q) {
		
		
		TIntArrayList toDel = new TIntArrayList();
		int i=-1;
		
		Visitor visitor = new Visitor()
		{
			@Override
			public boolean visit(Operator qt) {
				if(qt instanceof SingleTermOp)
				{
					return this.visit((SingleTermOp)qt);
				}
				else if(qt instanceof MultiTermOp)
				{
					return this.visit((MultiTermOp)qt);
				}
				return true;
			}
			
			@Override
			public boolean visit(SingleTermOp sqt) {
				String origTerm = sqt.getTerm();
				String newTerm = tpa.pipelineTerm(origTerm);
				if (newTerm == null)
					return false;
				sqt.setTerm(newTerm);
				return true;
			}

			@Override
			public boolean visit(MultiTermOp mqt) {
				Operator[] qts = mqt.getConstituents();
				boolean OK = true;
				for(Operator qt : qts) {
					//boolean OKqt = 
					this.visit(qt);
				}
				//TODO check if all required?
				return OK;
			}
			
		};
		
		MatchingQueryTerms mqt = q.getMatchingQueryTerms();		
		for(MatchingTerm t : mqt)
		{
			i++;
			boolean OK = visitor.visit(t.getKey());
			if (! OK)
				toDel.add(i);
		}
		toDel.reverse();
		for(int removeIndex : toDel.toNativeArray())
		{
			mqt.remove(removeIndex);
		}
	}

	@Override
	public String getInfo() {
		return this.getClass().getSimpleName() + '(' + this.info + ')';
	}

}