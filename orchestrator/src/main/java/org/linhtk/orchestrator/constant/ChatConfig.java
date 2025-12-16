package org.linhtk.orchestrator.constant;

public final class ChatConfig {
    public static final String SUMMARY_PROMPT = "Summarize the following user question into a short title (max 7 words), no punctuation:";
    public static final int QUESTION_TYPE = 1;
    public static final int ANSWER_TYPE = 2;
    public static final int TOP_K = 5;
    public static final String PNG_CONTENT_TYPE = "image/png";
    public static final int MAXIMUM_UPLOAD_FILE_SIZE = 5 * 1024 * 1024;
    public static final String SEARCH_TOOL_INSTRUCTION = """
        When the tool response contains search results (titles, snippets, links):
        1. Use the snippets as factual evidence ONLY IF they are relevant to the user's question.
        2. Determine relevance by checking whether the snippet meaningfully answers, clarifies, or provides data directly related to the user query.
        3. If one or more results are relevant:
           - Synthesize a clean, conversational answer based on those relevant snippets.
           - Embed citations inline using Markdown format: [title](URL)
           - Apply citations ONLY to statements supported by relevant snippets.
           - Example:
               "It may rain lightly today [Baomoi](https://baomoi.com/abc)."
        4. If the search results exist but NONE of them are relevant:
           - Ignore the search results completely.
           - Provide a general, helpful answer WITHOUT any links.
        5. If the tool returns no results:
           - Provide a general answer based only on your internal knowledge.
        Rules:
        - Do NOT list citations at the end of the answer.
        - Do NOT attach a link to every sentence. Only cite where evidence is actually used.
        - Prefer the most relevant sources first (best snippet match).
        - Maintain a natural, conversational tone.
    """;
    public static final int CHATGPT_DIMENSION = 1536;
    public static final int GEMINI_DIMENSION = 768;
    public static final String SUMMARY_UPDATE_PROMPT = """
    You create a new running conversation summary.

    Goal:
    - Produce a short, factual summary based on:
      (1) The previous summary
      (2) The newest user/assistant message

    Given the recent messages update the summary in a structured way.

    Rules:
    - Keep only information relevant for continuing the conversation.
    - Track multiple topics separately.
    - Keep a list of unresolved questions.
    - Preserve the user's preferences.
    - Remove obsolete or irrelevant threads.

    Output format MUST follow this structure:

    ACTIVE_TOPICS:
      - <topic1>:
          * fact or decision
          * fact or detail
      - <topic2>:
          * ...

    OPEN_QUESTIONS:
      - <list or empty>

    USER_PREFERENCES:
      - <list or empty>

    Recent message:
    {{latest_message}}

    Return the updated summary.
    """;
    public static final String SYSTEM_PROMPT = """
If you are unsure about an answer or the information is not available in the provided context, respond politely and constructively.
Offer clarification, ask follow-up questions, or provide helpful guidance instead of saying “I don’t know.”
Always aim to assist the user by explaining what can be inferred, what additional details are needed, or what alternative steps they can take.
""";
}
