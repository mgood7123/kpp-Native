# nonterminal-symbol
class NonTerminal
	attr_reader :name	   # String
	attr_accessor :rules	# Production rules for this symbol. Array of Arrays of Strings or other NonTerminal instances
	def initialize(n)
		@name = n
		@rules = []
	end
	def <<(r)
		@rules << r
	end
	def to_s
		"<" + @name + ">"
	end
	def inspect
		to_s
	end
end

# A parsed nonterminal symbol
class NTermInst
	attr_accessor :nonTerminal  # The accompanying NonTerminal instance
	attr_accessor :decision	 # Index of the production rule applied to get the parsing result ( =index of the child in the decision-tree)
	attr_accessor :children	 # Instances of the symbols produced after applying the production rule; Array of Strings and more NTermInst-Instances
	def initialize(nt)
		@nonTerminal = nt
		@decision = -1
		@children = []
	end
	# Recursive textbased representation (Parser-Tree)
	def to_s(depth = 0)
		ind = "  " * depth
		ind + "[#{nonTerminal}/#{decision}" +
		if(children.size == 0)
			"]\n"
		else
			"\n" + children.map { |c| if(c.is_a?(String)) then ind + "  " + c.inspect + "\n" else c.to_s(depth+1) end }.join("") + ind + "]\n"
		end
	end
	def inspect
		to_s
	end
	# Search all child elements recursively which are instances of the nonterminal symbol contNT, collect all contained child elements
	# which are of type childclass and return them as an array. childclass can be a ruby-class or a NonTerminal instance.
	# Used to read in recursive grammar-definitions as a flat array
	def collectRecursive(contNT, childclass)
		@children.select { |c|
			(childclass.is_a?(Class) && c.is_a?(childclass)) ||
				(childclass.is_a?(NonTerminal) && c.is_a?(NTermInst) && c.nonTerminal == childclass) } +
			@children.select { |c| c.is_a?(NTermInst) && c.nonTerminal == contNT }.inject([]) { |old,obj| old+obj.collectRecursive(contNT, childclass) }
	end
	# Recursively collect all read in strings ( of Terminal symbols ) and return them as one ruby string
	# Used to get the textual representation of this instance in the parsed string
	def collectString
		@children.map { |c| if(c.is_a?(String)) then c else c.collectString end }.join("")
	end
	def [](ind)
		@children[ind]
	end
end

# Represents a branch in the decision-tree of the non-deterministic stack machine (NPDA). Is managed via a query to run a breadth first search
# on that tree.
class DNode
	attr_accessor :nonTerminal	  # The nonterminal symbol, whose rules are the subject of the decision of this node in the tree
	attr_accessor :index			# Currenty processed decision (index of the NonTerminal#rules Array)
	attr_accessor :strIndex		 # Index in input string
	attr_accessor :stack			# Stack at the point of the decision
	attr_accessor :pnode			# Previous(parent) branch
	attr_accessor :pindex		   # Decision that was made in the parent branch to reach this branch
	def initialize(n, i, s, si, pn)
		@nonTerminal = n
		@index = i
		@stack = s
		@strIndex = si
		@pnode = pn
		if(pn != nil)
			@pindex = pn.index
		end
	end
end

# Represents a grammar as ruby-objects. Hash of names of nonterminal symbols to the corresponding NonTerminal instances
class Grammar < Hash
	attr_accessor :start	# Start symbol (initial content of the stack)
	def initialize(nn,&block)
		nn.each { |n| self[n] = NonTerminal.new(n) }
		@start = nil
		if(block != nil) then block.call(self) end
	end
	# Parse string with this grammar. Returns nil in case of non-acceptance, else the root of the parser tree as a DNode-instance else
	def parse(str)
		queue = []		  # Queue of DNode-Instances, for the breadth first search in the decision tree
		stack = [start]	 # Stack of the NPDA
		index = 0		   # Position in the input string
#		debug "STACK #{stack}\n"
	   
		while(!stack.empty? || index != str.length)
			if(!stack.empty? && stack.last.is_a?(String) && (stack.last.length <= str.length-index) && str[index...index+stack.last.length] == stack.last)
				# Accept-away the string (advance)
				top = stack.pop
#				debug("Akzeptiere String #{top.inspect}\n")
				index += top.length
			elsif(!stack.empty? && stack.last.is_a?(NonTerminal) && stack.last.rules.size == 1)
				# Apply rule directly, no decision neccessary
				top = stack.pop
#				debug "#{top} : Direkt ersetzen -> #{top.rules[0]}\n"
				stack.concat(top.rules[0].reverse)
			else
				if(!stack.empty? && stack.last.is_a?(NonTerminal))
					# Queue decision
					top = stack.pop
#					debug "#{top} : Einreihen\n"
					queue.push(DNode.new(top, -1, stack, index, if(queue.empty?) then nil else queue.first end))
				else
					# Dead end - forget stack
				end


				# Apply nex dicision in the queue
				if(queue.empty? || (queue.first.index == queue.first.nonTerminal.rules.length - 1 && queue.size < 2))
					# Can't continue in the current path(branch), no other existing paths => Don't accept string
					return nil
				else
					decide = queue.first
					decide.index = decide.index+1
					# Last decision for this DNode-object; take the next from the queue
					if(decide.index == decide.nonTerminal.rules.length)
#						debug("#{queue.first.nonTerminal} : Dead end go to #{queue[1].nonTerminal}/#{queue[1].index+1}")
						queue.delete_at(0)
						decide = queue.first
						decide.index = decide.index+1
						
#						debug("#{decide.nonTerminal}: Last Decision")
					else
						# Next decision for this branch
#						debug "#{decide.nonTerminal}: Decide #{decide.index}"
					end
					# Use the stack of this branch
					stack = decide.stack.clone
#					debug " ++ #{decide.nonTerminal.rules[decide.index]}\n"
					# Decided -> Apply production rule (Fill stack)
					stack.concat(decide.nonTerminal.rules[decide.index].reverse)
					# Jump to the position of this branch in the input string
					index = decide.strIndex
				end
			end
#			debug "STACK #{stack}\n"
		end
		# Backtrace what decision was applied when, to generate array of indices of decisions (backwards)
		trace = []
		if(!queue.empty?)
			d = queue.first
			trace << d.index
			while(d.pnode != nil)
				trace << d.pindex
				d = d.pnode
			end
		end
	   
		# Generate parser tree
		root = NTermInst.new(start)
		stack = [root]
		while(!stack.empty?)
			top = stack.pop()
			# What decision was done here
			top.decision = if(top.nonTerminal.rules.size>1) then trace.pop() else 0 end
		   
			s = top.nonTerminal.rules[top.decision].size
			top.children = Array.new(s)
		   
			# For all symbols on the right side of this production rule...
			(s-1).downto(0) { |i|
				sym = top.nonTerminal.rules[top.decision][i]
				top.children[i] = if(sym.is_a?(String))
					# String ( = Sequence of terminal symbols) => Put into parser tree
					sym
				else
					# Non terminal symbols => to stack
					x = NTermInst.new(sym)
					stack.push(x)
					x
				end
			}
		end
	   
#		debug "Accepting\n"
		root # Parserbaum zurückgeben
	end
	# Return textual BNF of this grammar
	def to_s
		map { |k,v|
			v.to_s + " ::= " + v.rules.map { |r| r.map{|e| e.inspect}.join(" ") }.join(" | ")
		}.join("\n")
	end
	def inspect
		to_s
	end
#	def debug(str)
#	   $stdout.write(str)
#	end
	# Returns the Grammar of the modified BNF, which allows parsing arbitrary special chars
	def Grammar.BNF
		@@bnf ||= Grammar.new(["BNF", "BNF2", "Def", "Rules", "Rules2", "Rule", "Symbol", "String", "Chars", "Char", "NTerm", "Name", "Name2", "SpaceE", "Space", "Alpha", "Alnum"]) { |g|
			g["BNF"] <<  [g["Def"], g["BNF2"]]
			g["BNF2"].rules = [["\n", g["Def"], g["BNF2"]], [], ["\n", g["BNF2"]]]
			g["Def"] << [g["SpaceE"], g["NTerm"], g["SpaceE"], "::=", g["Rules"]]
			g["Rules"] << [g["Rule"], g["Rules2"]]
			g["Rules2"].rules= [["|", g["Rule"], g["Rules2"]], []]
			g["Rule"] << [g["SpaceE"]]
			g["Rule"] << [g["SpaceE"], g["Symbol"], g["Rule"]]

			g["Symbol"].rules = [[g["String"]], [g["NTerm"]]]
			g["String"] << ["\"", g["Chars"], "\""]
			g["Chars"] << []
			g["Chars"] << [g["Char"], g["Chars"]]
			for i in 0..255 do
				g["Char"] << [[i].pack("C").inspect[1..-2]]
			end

			g["NTerm"] << ["<", g["Name"], ">"]
			g["Name"] << [g["Alpha"]]
			g["Name"] << [g["Alpha"], g["Name2"]]
			g["Name2"] << [g["Alnum"], g["Name2"]]
			g["Name2"] << []

			g["SpaceE"] << [g["Space"]]
			g["SpaceE"] << []
			g["Space"] << [" ",  g["SpaceE"]]
			g["Space"] << ["\t", g["SpaceE"]]
			g["Space"] << ["\r", g["SpaceE"]]

			for i in 0..25 do
				a = [[[65 + i].pack("C")], [[97 + i].pack("C")]]
				g["Alpha"].rules.concat(a)
				g["Alnum"].rules.concat(a)
			end
			for i in 0..9 do
				g["Alnum"] << [[48 + i].pack("C")]
			end
			g.start = g["BNF"]
		}
	end
	# Parse given grammar in BNF and return as instance of 'Grammar'. Uses the grammar for BNF.
	def Grammar.fromBNF(str)
		b = Grammar.BNF
		s = nil
		defs = b.parse(str).collectRecursive(b["BNF2"], b["Def"])
		g = Grammar.new(defs.map { |df| df[1][1].collectString })
		defs.each { |df|
#		   puts "foobar"
			name = df[1][1].collectString
			nt = g[name]
			if(s == nil) then s = nt end		# Use the 1st defined Nonterminal symbol as start symbol
			nt.rules = df[4].collectRecursive(b["Rules2"], b["Rule"]).map { |rule|
				rule.collectRecursive(b["Rule"], b["Symbol"]).map { |sym|
					if(sym.decision == 0)
						eval(sym[0].collectString)
					else
						g[sym[0][1].collectString]
					end
				}
			}
		}
		g.start = s
		g
	end
end

# == Example ==

# Give grammar for arithmetic expressions in BNF and parse it
# \t, \r are escaped because of ruby
arith = Grammar.fromBNF(<<BNF)
<E> ::= "(" <SpaceE> <E> <SpaceE> <Infix> <SpaceE> <E> <SpaceE> ")" | <Fun> <SpaceE> "(" <SpaceE> <E> <SpaceE> ")" | <Literal>
<Infix> ::= "+" | "-" | "*" | "/" | "%"
<Fun> ::= "sin" | "cos" | "sqrt"
<Literal> ::= <Digits> | <Digits> "." <Digits>
<Digit> ::= "0" | "1" | "2" | "3" | "4" | "5" | "6" | "7" | "8" | "9"
<Digits> ::= <Digit> <DigitsE>
<DigitsE> ::= <Digit> <DigitsE> |
<SpaceE> ::= <Space> |
<Space> ::= " " <SpaceE> | "\\t" <SpaceE> | "\\r" <SpaceE>
BNF

puts arith.to_s

# Parse expression and output parse tree
expr = "((3+(7.9831*sqrt(33.5)))-sin(0.1234))"
puts arith.parse(expr).inspect
