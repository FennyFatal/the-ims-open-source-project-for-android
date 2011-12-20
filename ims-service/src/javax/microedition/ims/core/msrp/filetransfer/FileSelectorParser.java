
// line 1 "FileSelectorParser.rl"
/*
 * This software code is (c) 2010 T-Mobile USA, Inc. All Rights Reserved.
 *
 * Unauthorized redistribution or further use of this material is
 * prohibited without the express permission of T-Mobile USA, Inc. and
 * will be prosecuted to the fullest extent of the law.
 *
 * Removal or modification of these Terms and Conditions from the source
 * or binary code of this software is prohibited.  In the event that
 * redistribution of the source or binary code for this software is
 * approved by T-Mobile USA, Inc., these Terms and Conditions and the
 * above copyright notice must be reproduced in their entirety and in all
 * circumstances.
 *
 * No name or trademarks of T-Mobile USA, Inc., or of its parent company,
 * Deutsche Telekom AG or any Deutsche Telekom or T-Mobile entity, may be
 * used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" AND "WITH ALL FAULTS" BASIS
 * AND WITHOUT WARRANTIES OF ANY KIND.  ALL EXPRESS OR IMPLIED
 * CONDITIONS, REPRESENTATIONS OR WARRANTIES, INCLUDING ANY IMPLIED
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, OR
 * NON-INFRINGEMENT CONCERNING THIS SOFTWARE, ITS SOURCE OR BINARY CODE
 * OR ANY DERIVATIVES THEREOF ARE HEREBY EXCLUDED.  T-MOBILE USA, INC.
 * AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY
 * LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE
 * OR ITS DERIVATIVES.  IN NO EVENT WILL T-MOBILE USA, INC. OR ITS
 * LICENSORS BE LIABLE FOR LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT,
 * INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES,
 * HOWEVER CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT
 * OF THE USE OF OR INABILITY TO USE THIS SOFTWARE, EVEN IF T-MOBILE USA,
 * INC. HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 *
 * THESE TERMS AND CONDITIONS APPLY SOLELY AND EXCLUSIVELY TO THE USE,
 * MODIFICATION OR DISTRIBUTION OF THIS SOFTWARE, ITS SOURCE OR BINARY
 * CODE OR ANY DERIVATIVES THEREOF, AND ARE SEPARATE FROM ANY WRITTEN
 * WARRANTY THAT MAY BE PROVIDED WITH A DEVICE YOU PURCHASE FROM T-MOBILE
 * USA, INC., AND TO THE EXTENT PERMITTED BY LAW.
 */

package javax.microedition.ims.core.msrp.filetransfer;

import java.util.ArrayList;
import java.util.List;

import javax.microedition.ims.messages.parser.ParserUtils;
public class FileSelectorParser{


// line 129 "FileSelectorParser.rl"



// line 4 "FileSelectorParser.java"
private static byte[] init__selector_actions_0()
{
	return new byte [] {
	    0,    1,    0,    1,    1,    1,    2,    1,    3,    1,    4,    1,
	    5,    2,    0,    5,    2,    1,    0,    2,    1,    5,    3,    1,
	    0,    5
	};
}

private static final byte _selector_actions[] = init__selector_actions_0();


private static short[] init__selector_key_offsets_0()
{
	return new short [] {
	    0,    0,    4,    5,    6,    7,    8,    9,   10,   11,   12,   13,
	   14,   20,   26,   32,   33,   34,   35,   36,   37,   38,   39,   40,
	   41,   42,   48,   54,   60,   61,   62,   63,   64,   65,   74,   82,
	   84,   85,   86,   87,   88,   90,   94,   95,   96,   97,   98,  100,
	  103,  105,  109,  115,  121,  122,  123,  124,  125,  126,  135,  143,
	  145,  151,  157,  158,  159,  160,  161,  163,  167,  168,  169,  170,
	  171,  173,  176,  178,  182,  187,  192,  196
	};
}

private static final short _selector_key_offsets[] = init__selector_key_offsets_0();


private static char[] init__selector_trans_keys_0()
{
	return new char [] {
	  104,  110,  115,  116,   97,  115,  104,   58,  115,  104,   97,   45,
	   49,   58,   48,   57,   65,   70,   97,  102,   48,   57,   65,   70,
	   97,  102,   48,   57,   65,   70,   97,  102,   97,  115,  104,   58,
	  115,  104,   97,   45,   49,   58,   48,   57,   65,   70,   97,  102,
	   48,   57,   65,   70,   97,  102,   48,   57,   65,   70,   97,  102,
	   97,  109,  101,   58,   34,   37,    1,    9,   11,   12,   14,   33,
	   35,  255,   34,   37,    1,    9,   11,   12,   14,  255,    9,   32,
	  105,  122,  101,   58,   48,   57,    9,   32,   48,   57,  121,  112,
	  101,   58,   33,  126,   47,   33,  126,   33,  126,    9,   32,   33,
	  126,   48,   57,   65,   70,   97,  102,   48,   57,   65,   70,   97,
	  102,   97,  109,  101,   58,   34,   37,    1,    9,   11,   12,   14,
	   33,   35,  255,   34,   37,    1,    9,   11,   12,   14,  255,    9,
	   32,   48,   57,   65,   70,   97,  102,   48,   57,   65,   70,   97,
	  102,  105,  122,  101,   58,   48,   57,    9,   32,   48,   57,  121,
	  112,  101,   58,   33,  126,   47,   33,  126,   33,  126,    9,   32,
	   33,  126,   58,  104,  110,  115,  116,   58,  104,  110,  115,  116,
	  104,  110,  115,  116,  104,  110,  115,  116,    0
	};
}

private static final char _selector_trans_keys[] = init__selector_trans_keys_0();


private static byte[] init__selector_single_lengths_0()
{
	return new byte [] {
	    0,    4,    1,    1,    1,    1,    1,    1,    1,    1,    1,    1,
	    0,    0,    0,    1,    1,    1,    1,    1,    1,    1,    1,    1,
	    1,    0,    0,    0,    1,    1,    1,    1,    1,    1,    2,    2,
	    1,    1,    1,    1,    0,    2,    1,    1,    1,    1,    0,    1,
	    0,    2,    0,    0,    1,    1,    1,    1,    1,    1,    2,    2,
	    0,    0,    1,    1,    1,    1,    0,    2,    1,    1,    1,    1,
	    0,    1,    0,    2,    5,    5,    4,    4
	};
}

private static final byte _selector_single_lengths[] = init__selector_single_lengths_0();


private static byte[] init__selector_range_lengths_0()
{
	return new byte [] {
	    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
	    3,    3,    3,    0,    0,    0,    0,    0,    0,    0,    0,    0,
	    0,    3,    3,    3,    0,    0,    0,    0,    0,    4,    3,    0,
	    0,    0,    0,    0,    1,    1,    0,    0,    0,    0,    1,    1,
	    1,    1,    3,    3,    0,    0,    0,    0,    0,    4,    3,    0,
	    3,    3,    0,    0,    0,    0,    1,    1,    0,    0,    0,    0,
	    1,    1,    1,    1,    0,    0,    0,    0
	};
}

private static final byte _selector_range_lengths[] = init__selector_range_lengths_0();


private static short[] init__selector_index_offsets_0()
{
	return new short [] {
	    0,    0,    5,    7,    9,   11,   13,   15,   17,   19,   21,   23,
	   25,   29,   33,   37,   39,   41,   43,   45,   47,   49,   51,   53,
	   55,   57,   61,   65,   69,   71,   73,   75,   77,   79,   85,   91,
	   94,   96,   98,  100,  102,  104,  108,  110,  112,  114,  116,  118,
	  121,  123,  127,  131,  135,  137,  139,  141,  143,  145,  151,  157,
	  160,  164,  168,  170,  172,  174,  176,  178,  182,  184,  186,  188,
	  190,  192,  195,  197,  201,  207,  213,  218
	};
}

private static final short _selector_index_offsets[] = init__selector_index_offsets_0();


private static byte[] init__selector_indicies_0()
{
	return new byte [] {
	    0,    2,    3,    4,    1,    5,    1,    6,    1,    7,    1,    8,
	    1,    9,    1,   10,    1,   11,    1,   12,    1,   13,    1,   14,
	    1,   15,   15,   15,    1,   16,   16,   16,    1,   17,   17,   17,
	    1,   18,    1,   19,    1,   20,    1,   21,    1,   22,    1,   23,
	    1,   24,    1,   25,    1,   26,    1,   27,    1,   28,   28,   28,
	    1,   29,   29,   29,    1,   30,   30,   30,    1,   31,    1,   32,
	    1,   33,    1,   34,    1,   35,    1,   37,   36,   36,   36,   36,
	    1,   39,   40,   38,   38,   38,    1,   41,   41,    1,   42,    1,
	   43,    1,   44,    1,   45,    1,   46,    1,   47,   47,   48,    1,
	   49,    1,   50,    1,   51,    1,   52,    1,   53,    1,   55,   54,
	    1,   56,    1,   57,   57,   56,    1,   58,   58,   58,    1,   38,
	   38,   38,    1,   59,    1,   60,    1,   61,    1,   62,    1,   63,
	    1,   65,   64,   64,   64,   64,    1,   67,   68,   66,   66,   66,
	    1,   69,   69,    1,   70,   70,   70,    1,   66,   66,   66,    1,
	   71,    1,   72,    1,   73,    1,   74,    1,   75,    1,   76,   76,
	   77,    1,   78,    1,   79,    1,   80,    1,   81,    1,   82,    1,
	   84,   83,    1,   85,    1,   86,   86,   85,    1,   87,   88,   89,
	   90,   91,    1,   92,   93,   94,   95,   96,    1,   97,   98,   99,
	  100,    1,  101,  102,  103,  104,    1,    0
	};
}

private static final byte _selector_indicies[] = init__selector_indicies_0();


private static byte[] init__selector_trans_targs_0()
{
	return new byte [] {
	    2,    0,   52,   62,   68,    3,    4,    5,    6,    7,    8,    9,
	   10,   11,   12,   13,   76,   13,   16,   17,   18,   19,   20,   21,
	   22,   23,   24,   25,   26,   77,   26,   29,   30,   31,   32,   33,
	   34,   50,   34,   35,   50,   78,   37,   38,   39,   40,   41,   78,
	   41,   43,   44,   45,   46,   47,   47,   48,   49,   78,   51,   53,
	   54,   55,   56,   57,   58,   60,   58,   59,   60,   79,   61,   63,
	   64,   65,   66,   67,   79,   67,   69,   70,   71,   72,   73,   73,
	   74,   75,   79,   14,   15,   28,   36,   42,   27,   15,   28,   36,
	   42,   15,   28,   36,   42,   15,   28,   36,   42
	};
}

private static final byte _selector_trans_targs[] = init__selector_trans_targs_0();


private static byte[] init__selector_trans_actions_0()
{
	return new byte [] {
	    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
	    0,    0,    0,    1,    0,    0,    0,    0,    0,    0,    0,    0,
	    0,    0,    0,    0,    1,    0,    0,    0,    0,    0,    0,    0,
	    1,    1,    0,    5,    0,    0,    0,    0,    0,    0,    1,    9,
	    0,    0,    0,    0,    0,    1,    0,    0,    0,    7,    0,    0,
	    0,    0,    0,    0,    1,    1,    0,    5,    0,    0,    0,    0,
	    0,    0,    0,    1,    9,    0,    0,    0,    0,    0,    1,    0,
	    0,    0,    7,    0,   16,   16,   16,   16,    0,    3,    3,    3,
	    3,    0,    0,    0,    0,    1,    1,    1,    1
	};
}

private static final byte _selector_trans_actions[] = init__selector_trans_actions_0();


private static byte[] init__selector_eof_actions_0()
{
	return new byte [] {
	    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
	    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
	    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
	    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
	    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
	    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
	    0,    0,    0,    0,   22,   19,   11,   13
	};
}

private static final byte _selector_eof_actions[] = init__selector_eof_actions_0();


static final int selector_start = 1;
static final int selector_first_final = 76;
static final int selector_error = 0;

static final int selector_en_main = 1;


// line 132 "FileSelectorParser.rl"

	protected static String arrayToString(int mark, int p) {
		byte[] tmp = new byte[ p - mark ];
		System.arraycopy( data, mark, tmp, 0, p - mark);
		String result = new String( tmp );
		System.out.println(result);
		return result;
	}

	protected static byte[] data;

	public static List<FileDescriptor> parse(String input) {
		try {
            data = input.getBytes("UTF-8");
        } catch (Exception e) {
            System.out.println("Unsupported encoding");
            return null;
        }

        int m_Mark = 0;
        int cs = 0;                  // Ragel keeps the state in "cs"
        int p = 0;                   // Current index into data is "p"
        int pe = data.length;        // Length of data
        int eof = pe;

        List<FileDescriptor> descriptors = new ArrayList<FileDescriptor>();

	String type = null;
	    String name = null;
		String hash = null;
		int size = 0;


// line 194 "FileSelectorParser.java"
	{
	cs = selector_start;
	}

// line 165 "FileSelectorParser.rl"

// line 199 "FileSelectorParser.java"
	{
	int _klen;
	int _trans = 0;
	int _acts;
	int _nacts;
	int _keys;
	int _goto_targ = 0;

	_goto: while (true) {
	switch ( _goto_targ ) {
	case 0:
	if ( p == pe ) {
		_goto_targ = 4;
		continue _goto;
	}
	if ( cs == 0 ) {
		_goto_targ = 5;
		continue _goto;
	}
case 1:
	_match: do {
	_keys = _selector_key_offsets[cs];
	_trans = _selector_index_offsets[cs];
	_klen = _selector_single_lengths[cs];
	if ( _klen > 0 ) {
		int _lower = _keys;
		int _mid;
		int _upper = _keys + _klen - 1;
		while (true) {
			if ( _upper < _lower )
				break;

			_mid = _lower + ((_upper-_lower) >> 1);
			if ( data[p] < _selector_trans_keys[_mid] )
				_upper = _mid - 1;
			else if ( data[p] > _selector_trans_keys[_mid] )
				_lower = _mid + 1;
			else {
				_trans += (_mid - _keys);
				break _match;
			}
		}
		_keys += _klen;
		_trans += _klen;
	}

	_klen = _selector_range_lengths[cs];
	if ( _klen > 0 ) {
		int _lower = _keys;
		int _mid;
		int _upper = _keys + (_klen<<1) - 2;
		while (true) {
			if ( _upper < _lower )
				break;

			_mid = _lower + (((_upper-_lower) >> 1) & ~1);
			if ( data[p] < _selector_trans_keys[_mid] )
				_upper = _mid - 2;
			else if ( data[p] > _selector_trans_keys[_mid+1] )
				_lower = _mid + 2;
			else {
				_trans += ((_mid - _keys)>>1);
				break _match;
			}
		}
		_trans += _klen;
	}
	} while (false);

	_trans = _selector_indicies[_trans];
	cs = _selector_trans_targs[_trans];

	if ( _selector_trans_actions[_trans] != 0 ) {
		_acts = _selector_trans_actions[_trans];
		_nacts = (int) _selector_actions[_acts++];
		while ( _nacts-- > 0 )
	{
			switch ( _selector_actions[_acts++] )
			{
	case 0:
// line 54 "FileSelectorParser.rl"
	{
		m_Mark = p;
	}
	break;
	case 1:
// line 58 "FileSelectorParser.rl"
	{
		hash = arrayToString(m_Mark, p);
	}
	break;
	case 2:
// line 62 "FileSelectorParser.rl"
	{
		name = arrayToString(m_Mark, p);
	}
	break;
	case 3:
// line 66 "FileSelectorParser.rl"
	{
		type = arrayToString(m_Mark, p);
	}
	break;
	case 4:
// line 70 "FileSelectorParser.rl"
	{
		size =  ParserUtils.toNumber(arrayToString(m_Mark, p), 0, 10);
	}
	break;
// line 309 "FileSelectorParser.java"
			}
		}
	}

case 2:
	if ( cs == 0 ) {
		_goto_targ = 5;
		continue _goto;
	}
	if ( ++p != pe ) {
		_goto_targ = 1;
		continue _goto;
	}
case 4:
	if ( p == eof )
	{
	int __acts = _selector_eof_actions[cs];
	int __nacts = (int) _selector_actions[__acts++];
	while ( __nacts-- > 0 ) {
		switch ( _selector_actions[__acts++] ) {
	case 0:
// line 54 "FileSelectorParser.rl"
	{
		m_Mark = p;
	}
	break;
	case 1:
// line 58 "FileSelectorParser.rl"
	{
		hash = arrayToString(m_Mark, p);
	}
	break;
	case 5:
// line 74 "FileSelectorParser.rl"
	{
	FileDescriptor fd = new FileDescriptor.FileDescriptorBuilder()
                .fileName(name)
                .contentType(type)
                .fileSize(size)
                .hash(hash)
                .build();
		descriptors.add(fd);
	}
	break;
// line 354 "FileSelectorParser.java"
		}
	}
	}

case 5:
	}
	break; }
	}

// line 166 "FileSelectorParser.rl"

	if (cs == selector_error) {
		 System.out.println("Your input did not comply with the grammar");
         System.out.println("selector parsing error at " + p);
		return null;
	} else if (cs < selector_first_final) {
		System.out.println("selector attribute incomplete");
		return null;
	} else {
	    System.out.println("selector parsed successfully!");
	    return descriptors;

	 }
}

public static void main(String[] args) {
    String input = "name:\"charlie.jpg\" type:foo/bar size:3660 hash:sha-1:ae:01:9e:d5:25:28:27:da:12:2d:fd:27:51:cb:f1:b3:da:9b:1f:be";
    List<FileDescriptor> mpa = parse(input);
    System.out.println(mpa);
}

}

