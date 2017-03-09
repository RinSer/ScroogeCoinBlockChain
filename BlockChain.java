import java.util.ArrayList;
import java.util.HashMap;

// Block Chain should maintain only limited block nodes to satisfy the functions
// You should not have all the blocks added to the block chain in memory 
// as it would cause a memory overflow.

public class BlockChain {
	
	/**
	 * 
	 * Helper class to store each
	 * block's auxiliary data
	 * @author rinser
	 *
	 */
	public class Link {
		private int height;
		private int nth_child;
		private int number_of_children;
		private Block block;
		
		public Link(Block data) {
			this.height = 0;
			this.number_of_children = 0;
			this.nth_child = 0;
			this.block = data;
		}
		
		public Link(Link parent, Block data) {
			this.height = parent.getHeight() + 1;
			this.number_of_children = 0;
			parent.addChild();
			this.nth_child = parent.getNumberOfChildren();
			this.block = data;
		}
		
		public void addChild() {
			this.number_of_children++;
		}
		
		public Block getBlock() {
			return this.block;
		}
		
		public ArrayList<Transaction> getTxs() {
			return this.block.getTransactions();
		}
		
		public byte[] getBlockHash() {
			return this.block.getHash();
		}
		
		public byte[] getParentHash() {
			return this.block.getPrevBlockHash();
		}
		
		public int getHeight() {
			return this.height;
		}
		
		public int getNumberOfChildren() {
			return this.number_of_children;
		}
		
		public int nthChild() {
			return this.nth_child;
		}
		
	}
	
    public static final int CUT_OFF_AGE = 10;
    public HashMap<byte[], Link> chain;
    public TransactionPool pool;

    /**
     * create an empty block chain with just a genesis block. Assume {@code genesisBlock} is a valid
     * block
     */
    public BlockChain(Block genesisBlock) {
        // IMPLEMENT THIS
    	this.chain = new HashMap<byte[], Link>();
    	this.pool = new TransactionPool();
    	this.chain.put(genesisBlock.getHash(), new Link(genesisBlock));
    }

    /** Get the maximum height block */
    public Block getMaxHeightBlock() {
        // IMPLEMENT THIS
    	int max_height = -1;
    	int max_child = 999999;
    	Link max_link = null;
    	for (Link current : this.chain.values()) {
    		if (current.getHeight() > max_height) {
    			max_height = current.getHeight();
    			max_link = current;
    		}
    		else if (current.getHeight() == max_height) {
    			if (current.nthChild() < max_child) {
    				max_child = current.nthChild();
    				max_link = current;
    			}
    		}
    	}
    	return max_link.getBlock();
    }

    /** Get the UTXOPool for mining a new block on top of max height block */
    public UTXOPool getMaxHeightUTXOPool() {
        // IMPLEMENT THIS
    	Block current = this.getMaxHeightBlock();
    	UTXOPool current_utxos = new UTXOPool();
    	if (current.getPrevBlockHash() == null) {
    		for (int t = -1; t < current.getTransactions().size(); t++) {
    			Transaction current_tx;
    			if (t == -1)
    				current_tx = current.getCoinbase();
    			else
    				current_tx = current.getTransaction(t);
        		for (int o = 0; o < current_tx.numOutputs(); o++) {
        			current_utxos.addUTXO(new UTXO(current_tx.getHash(), o), current_tx.getOutput(o));
        		}
        	}
    	}
    	while (current.getPrevBlockHash() != null) {
    		for (int t = -1; t < current.getTransactions().size(); t++) {
    			Transaction current_tx;
    			if (t == -1)
    				current_tx = current.getCoinbase();
    			else
    				current_tx = current.getTransaction(t);
        		for (int o = 0; o < current_tx.numOutputs(); o++) {
        			current_utxos.addUTXO(new UTXO(current_tx.getHash(), o), current_tx.getOutput(o));
        		}
        	}
    		current = this.chain.get(current.getPrevBlockHash()).getBlock();
    	}
    	
    	return current_utxos;
    }

    /** Get the transaction pool to mine a new block */
    public TransactionPool getTransactionPool() {
        // IMPLEMENT THIS
    	return pool;
    }

    /**
     * Add {@code block} to the block chain if it is valid. For validity, all transactions should be
     * valid and block should be at {@code height > (maxHeight - CUT_OFF_AGE)}.
     * 
     * <p>
     * For example, you can try creating a new block over the genesis block (block height 2) if the
     * block chain height is {@code <=
     * CUT_OFF_AGE + 1}. As soon as {@code height > CUT_OFF_AGE + 1}, you cannot create a new block
     * at height 2.
     * 
     * @return true if block is successfully added
     */
    public boolean addBlock(Block block) {
        // IMPLEMENT THIS
    	if (block.getPrevBlockHash() == null) {
    		return false;
    	}
    	else {
    		// Check the txs
    		UTXOPool block_pool = new UTXOPool();
    		if (this.chain.containsKey(block.getPrevBlockHash())) {
	    		Link parent = this.chain.get(block.getPrevBlockHash());
	    		if (parent.getParentHash() == null) {
	    			for (int t = -1; t < parent.getTxs().size(); t++) {
	            		Transaction current_tx;
	            		if (t == -1)
	            			current_tx = parent.getBlock().getCoinbase();
	            		else
	            			current_tx = parent.getBlock().getTransaction(t);
	            		for (int o = 0; o < current_tx.numOutputs(); o++) {
	            			block_pool.addUTXO(new UTXO(current_tx.getHash(), o), current_tx.getOutput(o));
	            		}
	            	}
	    		}
	    		while(parent.getParentHash() != null) {
	    			for (int t = -1; t < parent.getTxs().size(); t++) {
	            		Transaction current_tx;
	            		if (t == -1)
	            			current_tx = parent.getBlock().getCoinbase();
	            		else
	            			current_tx = parent.getBlock().getTransaction(t);
	            		for (int o = 0; o < current_tx.numOutputs(); o++) {
	            			block_pool.addUTXO(new UTXO(current_tx.getHash(), o), current_tx.getOutput(o));
	            		}
	            	}
	        		parent = this.chain.get(parent.getParentHash());
	    		}
	    		TxHandler block_handler = new TxHandler(block_pool);
	    		Transaction[] block_txs = new Transaction[block.getTransactions().size()];
	    		for (int i = 0; i < block.getTransactions().size(); i++) {
	    			block_txs[i] = block.getTransaction(i);
	    		}
	    		Transaction[] valid_txs = block_handler.handleTxs(block_txs);
	    		if (valid_txs.length == block_txs.length) {
	    			Link block_parent = this.chain.get(block.getPrevBlockHash());
	    			int max_height = this.chain.get(this.getMaxHeightBlock().getHash()).getHeight();
	    			if (block_parent.getHeight()+1 > max_height-CUT_OFF_AGE) {
	    				this.chain.put(block.getHash(), new Link(block_parent, block));
	    				return true;
	    			}
	    			else {
	    				return false;
	    			}
	    		}
	    		else {
	    			return false;
	    		}
    		}
    		else {
    			return false;
    		}
    	}
    }

    /** Add a transaction to the transaction pool */
    public void addTransaction(Transaction tx) {
        // IMPLEMENT THIS
    	this.pool.addTransaction(tx);
    }
}