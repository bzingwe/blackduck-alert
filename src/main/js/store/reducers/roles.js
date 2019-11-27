import {
    SERIALIZE,
    USER_MANAGEMENT_ROLE_DELETE_ERROR,
    USER_MANAGEMENT_ROLE_DELETED,
    USER_MANAGEMENT_ROLE_DELETING,
    USER_MANAGEMENT_ROLE_FETCH_ERROR_ALL,
    USER_MANAGEMENT_ROLE_FETCHED_ALL,
    USER_MANAGEMENT_ROLE_FETCHING_ALL,
    USER_MANAGEMENT_ROLE_SAVE_ERROR,
    USER_MANAGEMENT_ROLE_SAVED,
    USER_MANAGEMENT_ROLE_SAVING
} from 'store/actions/types'

const initialState = {
    inProgress: false,
    deleteSuccess: false,
    data: [],
    roleFetchError: '',
    roleSaveError: '',
    roleDeleteError: ''
};

const roles = (state = initialState, action) => {
    switch (action.type) {
        case USER_MANAGEMENT_ROLE_DELETE_ERROR:
            return Object.assign({}, state, {
                inProgress: false,
                deleteSuccess: false,
                roleDeleteError: action.roleDeleteError
            });
        case USER_MANAGEMENT_ROLE_DELETED:
            return Object.assign({}, state, {
                inProgress: false,
                deleteSuccess: true
            });
        case USER_MANAGEMENT_ROLE_DELETING:
            return Object.assign({}, state, {
                inProgress: true,
                deleteSuccess: false
            });
        case USER_MANAGEMENT_ROLE_FETCH_ERROR_ALL:
            return Object.assign({}, state, {
                inProgress: false,
                deleteSuccess: false,
                roleFetchError: action.roleFetchError
            });
        case USER_MANAGEMENT_ROLE_FETCHED_ALL:
            return Object.assign({}, state, {
                inProgress: false,
                deleteSuccess: false,
                data: action.data
            });
        case USER_MANAGEMENT_ROLE_FETCHING_ALL:
            return Object.assign({}, state, {
                inProgress: true,
                deleteSuccess: false,
                data: []
            });
        case USER_MANAGEMENT_ROLE_SAVE_ERROR:
            return Object.assign({}, state, {
                inProgress: false,
                deleteSuccess: false,
                roleSaveError: action.roleSaveError
            });
        case USER_MANAGEMENT_ROLE_SAVED:
            return Object.assign({}, state, {
                inProgress: false,
                deleteSuccess: false
            });
        case USER_MANAGEMENT_ROLE_SAVING:
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

export default roles;
