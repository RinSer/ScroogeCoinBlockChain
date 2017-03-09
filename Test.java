import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;

public class Test {

	public static void main(String[] args) throws NoSuchAlgorithmException, NoSuchProviderException, InvalidKeyException, SignatureException {
		
		// You need the following JAR for RSA http://www.bouncycastle.org/download/bcprov-jdk15on-156.jar
        // More information https://en.wikipedia.org/wiki/Bouncy_Castle_(cryptography)
        //Security.addProvider(new BouncyCastleProvider());
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        SecureRandom random = SecureRandom.getInstance("SHA1PRNG", "SUN");
        keyGen.initialize(1024, random);
 
        // Generating two key pairs, one for Scrooge and one for Alice
        KeyPair pair = keyGen.generateKeyPair();
        PrivateKey private_key_scrooge = pair.getPrivate();
        PublicKey public_key_scrooge = pair.getPublic();
        
        pair = keyGen.generateKeyPair();
        //PrivateKey private_key_alice = pair.getPrivate();
        PublicKey public_key_alice = pair.getPublic();
		
		//System.out.println("Process a transaction, create a block, process a transaction, create a block, ...");
		   
		   Block genesisBlock = new Block(null, public_key_scrooge);
		   genesisBlock.finalize();
		   
		   BlockChain blockChain = new BlockChain(genesisBlock);
		   BlockHandler blockHandler = new BlockHandler(blockChain);
		   
		   boolean passes = true;
		   Transaction spendCoinbaseTx;
		   /*Block prevBlock = genesisBlock;
		   
		   for (int i = 0; i < 20; i++) {
		      spendCoinbaseTx = new Transaction();
		      spendCoinbaseTx.addInput(prevBlock.getCoinbase().getHash(), 0);
		      spendCoinbaseTx.addOutput(Block.COINBASE, public_key_scrooge);
		      Signature signature = Signature.getInstance("SHA256withRSA");
		      signature.initSign(private_key_scrooge);
		      signature.update(spendCoinbaseTx.getRawDataToSign(0));
		      byte[] sig = signature.sign();
		      spendCoinbaseTx.addSignature(sig, 0);
		      spendCoinbaseTx.finalize();
		      blockHandler.processTx(spendCoinbaseTx);
		      
		      Block createdBlock = blockHandler.createBlock(public_key_scrooge);
		      
		      passes = passes && createdBlock != null && createdBlock.getPrevBlockHash().equals(prevBlock.getHash()) && createdBlock.getTransactions().size() == 1 && createdBlock.getTransaction(0).equals(spendCoinbaseTx);
		      prevBlock = createdBlock;
		   }
		   if (passes)
			   System.out.println("Passes");
		   */
		   // Test 25
		   System.out.println("Process multiple blocks directly on top of the genesis block, then create a block");
		   
		   genesisBlock = new Block(null, public_key_scrooge);
		   genesisBlock.finalize();
		      
		   blockChain = new BlockChain(genesisBlock);
		   blockHandler = new BlockHandler(blockChain);
		      
		   passes = true;
		   Block block = null;
		   Block firstBlock = null;
		      
		   for (int i = 0; i < 100; i++) {
		         block = new Block(genesisBlock.getHash(), public_key_alice);
		         if (i == 0)
		            firstBlock = block;
		         
		         spendCoinbaseTx = new Transaction();
		         spendCoinbaseTx.addInput(genesisBlock.getCoinbase().getHash(), 0);
		         spendCoinbaseTx.addOutput(Block.COINBASE, public_key_alice);
		         //spendCoinbaseTx.addSignature(people.get(0).getPrivateKey().sign(spendCoinbaseTx.getRawDataToSign(0)), 0);
		         Signature signature = Signature.getInstance("SHA256withRSA");
			     signature.initSign(private_key_scrooge);
			     signature.update(spendCoinbaseTx.getRawDataToSign(0));
			     byte[] sig = signature.sign();
			     spendCoinbaseTx.addSignature(sig, 0);
		         spendCoinbaseTx.finalize();
		         block.addTransaction(spendCoinbaseTx);
		         block.finalize();
		         passes = passes && blockHandler.processBlock(block);
		   }

		   Block createdBlock = blockHandler.createBlock(public_key_alice);
		   
		   System.out.println(passes);
		   System.out.println(createdBlock != null);
		   System.out.println(createdBlock.getPrevBlockHash().equals(firstBlock.getHash()));
		   System.out.println(createdBlock.getTransactions().size() == 0);
		   System.out.println(createdBlock != null && createdBlock.getPrevBlockHash().equals(firstBlock.getHash()) && createdBlock.getTransactions().size() == 0);
	}
	
}
