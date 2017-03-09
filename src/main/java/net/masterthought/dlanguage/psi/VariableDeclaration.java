package net.masterthought.dlanguage.psi;

/**
 * Created by francis on 3/1/2017.
 * Contains methods common to auto declarations and variable declarations
 */
public interface VariableDeclaration extends HasVisibility {

    /**
     * This method is needed because the bnf grammar cannot/does not always distinguish between x = 0; and int x = 0;
     *
     * @return whether or not this is a true variable declaration or a variable initialization only
     */
    boolean actuallyIsDeclaration();

    /**
     * todo change this to something more usable than dlanguageType, becuase not everything may return a existing dlanguage type.
     * ex: auto a = [7,7,9]; is of type int[] , but there is no int[] to return
     *
     * note: this method cannot be named getType becuase it conflicts with autogenerated methods
     * @return the type of the variable declaration if applicable
     */
    DLanguageType getVariableDeclarationType();


}
