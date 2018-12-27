package com.stripe.android;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.Size;
import android.support.annotation.VisibleForTesting;

import com.stripe.android.exception.StripeException;
import com.stripe.android.model.Card;
import com.stripe.android.model.Source;
import com.stripe.android.model.Token;

import java.util.Map;
import java.util.concurrent.Executor;

import static com.stripe.android.StripeNetworkUtils.hashMapFromCard;

public class Bongloy extends Stripe {
    private Context mContext;
    private BongloyApiHandler.LoggingResponseListener mLoggingResponseListener;
    private String mStripeAccount;

    /**
     * A constructor with only context, to set the key later.
     *
     * @param context {@link Context} for resolving resources
     */
    public Bongloy(@NonNull Context context) {
        super(context);
        mContext = context;
    }

    private class ResponseWrapper {
        final Source source;
        final Token token;
        final Exception error;

        private ResponseWrapper(Token token) {
            this.token = token;
            this.source = null;
            this.error = null;
        }

        private ResponseWrapper(Exception error) {
            this.error = error;
            this.source = null;
            this.token = null;
        }
    }

    private void tokenTaskPostExecution(Bongloy.ResponseWrapper result, TokenCallback callback) {
        if (result.token != null) {
            callback.onSuccess(result.token);
        } else if (result.error != null) {
            callback.onError(result.error);
        } else {
            callback.onError(new RuntimeException("Somehow got neither a token response or an " +
                    "error response"));
        }
    }

    private void executeTask(Executor executor, AsyncTask<Void, Void, ResponseWrapper> task) {
        if (executor != null) {
            task.executeOnExecutor(executor);
        } else {
            task.execute();
        }
    }

    @VisibleForTesting
    TokenCreator mTokenCreator = new TokenCreator() {
        @Override
        public void create(
                final Map<String, Object> tokenParams,
                final String publishableKey,
                final String stripeAccount,
                final @NonNull @Token.TokenType String tokenType,
                final Executor executor,
                final TokenCallback callback) {
            AsyncTask<Void, Void, ResponseWrapper> task =
                    new AsyncTask<Void, Void, ResponseWrapper>() {
                        @Override
                        protected Bongloy.ResponseWrapper doInBackground(Void... params) {
                            try {
                                RequestOptions requestOptions =
                                        RequestOptions.builder(
                                                publishableKey,
                                                stripeAccount,
                                                RequestOptions.TYPE_QUERY).build();
                                Token token = BongloyApiHandler.createToken(
                                        tokenParams,
                                        requestOptions);
                                return new Bongloy.ResponseWrapper(token);
                            } catch (StripeException e) {
                                return new Bongloy.ResponseWrapper(e);
                            }
                        }

                        @Override
                        protected void onPostExecute(Bongloy.ResponseWrapper result) {
                            tokenTaskPostExecution(result, callback);
                        }
                    };

            executeTask(executor, task);
        }
    };

    /**
     * Call to create a {@link Token} with the publishable key and {@link Executor} specified.
     *
     * @param card the {@link Card} used for this token
     * @param publishableKey the publishable key to use
     * @param executor an {@link Executor} to run this operation on. If null, this is run on a
     *                 default non-ui executor
     * @param callback a {@link TokenCallback} to receive the result or error message
     */

    public void createToken(
            @NonNull final Card card,
            @NonNull @Size(min = 1) final String publishableKey,
            @Nullable final Executor executor,
            @NonNull final TokenCallback callback) {

        mTokenCreator.create(
                hashMapFromCard(mContext, card),
                publishableKey,
                mStripeAccount,
                Token.TYPE_CARD,
                executor,
                callback);
    }
}
