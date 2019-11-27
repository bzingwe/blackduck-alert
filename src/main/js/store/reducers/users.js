import {
    SERIALIZE,
    USER_MANAGEMENT_USER_DELETE_ERROR,
    USER_MANAGEMENT_USER_DELETED,
    USER_MANAGEMENT_USER_DELETING,
    USER_MANAGEMENT_USER_FETCH_ERROR_ALL,
    USER_MANAGEMENT_USER_FETCHED_ALL,
    USER_MANAGEMENT_USER_FETCHING_ALL,
    USER_MANAGEMENT_USER_SAVE_ERROR,
    USER_MANAGEMENT_USER_SAVED,
    USER_MANAGEMENT_USER_SAVING
} from 'store/actions/types'

const initialState = {
    inProgress: false,
    deleteSuccess: false,
    data: [],
    userFetchError: '',
    userSaveError: '',
    userDeleteError: ''
};

const users = (state = initialState, action) => {
    switch (action.type) {
        case USER_MANAGEMENT_USER_DELETE_ERROR:
            return Object.assign({}, state, {
                inProgress: false,
                deleteSuccess: false,
                userDeleteError: action.userDeleteError
            });
        case USER_MANAGEMENT_USER_DELETED:
            return Object.assign({}, state, {
                inProgress: false,
                deleteSuccess: true
            });
        case USER_MANAGEMENT_USER_DELETING:
            return Object.assign({}, state, {
                inProgress: true,
                deleteSuccess: false
            });
        case USER_MANAGEMENT_USER_FETCH_ERROR_ALL:
            return Object.assign({}, state, {
                inProgress: false,
                deleteSuccess: false,
                userFetchError: action.userFetchError
            });
        case USER_MANAGEMENT_USER_FETCHED_ALL:
            return Object.assign({}, state, {
                inProgress: false,
                deleteSuccess: false,
                data: action.data
            });
        case USER_MANAGEMENT_USER_FETCHING_ALL:
            return Object.assign({}, state, {
                inProgress: true,
                deleteSuccess: false,
                data: []
            });
        case USER_MANAGEMENT_USER_SAVE_ERROR:
            return Object.assign({}, state, {
                inProgress: false,
                deleteSuccess: false,
                userSaveError: action.userSaveError
            });
        case USER_MANAGEMENT_USER_SAVED:
            return Object.assign({}, state, {
                inProgress: false,
                deleteSuccess: false
            });
        case USER_MANAGEMENT_USER_SAVING:
            return Object.assign({}, state, {
                inProgress: true,
                deleteSuccess: false
            });
        case SERIALIZE:
            return initialState;

        default:
            return state;
    }
};

export default users;
